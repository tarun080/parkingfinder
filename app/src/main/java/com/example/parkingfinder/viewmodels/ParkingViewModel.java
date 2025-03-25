package com.example.parkingfinder.viewmodels;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.parkingfinder.database.AppDatabase;
import com.example.parkingfinder.database.dao.ParkingAreaDao;
import com.example.parkingfinder.database.entities.ParkingAreaEntity;
import com.example.parkingfinder.firebase.FirebaseAuthManager;
import com.example.parkingfinder.firebase.FirestoreManager;
import com.example.parkingfinder.firebase.RealtimeDbManager;
import com.example.parkingfinder.models.ParkingArea;
import com.example.parkingfinder.models.ParkingSpot;
import com.example.parkingfinder.utils.Constants;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ParkingViewModel extends AndroidViewModel {
    private static final String TAG = "ParkingViewModel";

    // Firebase managers
    private FirebaseAuthManager authManager;
    private FirestoreManager firestoreManager;
    private RealtimeDbManager realtimeDbManager;

    // Database access
    private ParkingAreaDao parkingAreaDao;
    private ExecutorService databaseExecutor;

    // LiveData objects
    private MutableLiveData<List<ParkingArea>> parkingAreas = new MutableLiveData<>();
    private MutableLiveData<ParkingArea> selectedParkingArea = new MutableLiveData<>();
    private MutableLiveData<List<ParkingSpot>> parkingSpots = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();

    // State variables
    private double currentLatitude = 0;
    private double currentLongitude = 0;
    private ValueEventListener spotsListener;

    public ParkingViewModel(@NonNull Application application) {
        super(application);

        // Initialize Firebase managers
        authManager = FirebaseAuthManager.getInstance();
        firestoreManager = FirestoreManager.getInstance();
        realtimeDbManager = RealtimeDbManager.getInstance();

        // Initialize database access
        AppDatabase database = AppDatabase.getDatabase(application);
        parkingAreaDao = database.parkingAreaDao();
        databaseExecutor = Executors.newSingleThreadExecutor();

        // Initialize LiveData
        parkingAreas.setValue(new ArrayList<>());
        parkingSpots.setValue(new ArrayList<>());
    }

    // Getters for LiveData
    public LiveData<List<ParkingArea>> getParkingAreas() {
        return parkingAreas;
    }

    public LiveData<ParkingArea> getSelectedParkingArea() {
        return selectedParkingArea;
    }

    public LiveData<List<ParkingSpot>> getParkingSpots() {
        return parkingSpots;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    /**
     * Load nearby parking areas based on user location
     */
    public void loadParkingAreas(double latitude, double longitude, double radiusInKm) {
        isLoading.setValue(true);
        currentLatitude = latitude;
        currentLongitude = longitude;

        // First try to get from local database while fetching from server
        loadFromLocalDatabase(latitude, longitude);

        // Then fetch from Firestore
        firestoreManager.getNearbyParkingAreas(latitude, longitude, radiusInKm,
                new FirestoreManager.GetParkingAreasCallback() {
                    @Override
                    public void onSuccess(List<ParkingArea> areas) {
                        parkingAreas.setValue(areas);
                        isLoading.setValue(false);

                        // Cache results in local database
                        saveToLocalDatabase(areas);

                        // Update favorites status
                        if (authManager.isUserLoggedIn()) {
                            updateFavoritesStatus(areas);
                        }
                    }

                    @Override
                    public void onFailure(String message) {
                        errorMessage.setValue("Error loading parking areas: " + message);
                        isLoading.setValue(false);
                    }
                });
    }

    /**
     * Load parking spots for a specific parking area
     */
    public void loadParkingSpots(String parkingAreaId) {
        isLoading.setValue(true);

        // Remove any existing listener
        if (spotsListener != null) {
            realtimeDbManager.removeParkingSpotsListener(parkingAreaId, spotsListener);
            spotsListener = null;
        }

        // Set up real-time listener for parking spots
        spotsListener = realtimeDbManager.addParkingSpotsListener(parkingAreaId,
                new RealtimeDbManager.ParkingSpotsListener() {
                    @Override
                    public void onParkingSpotsUpdated(List<ParkingSpot> spots) {
                        parkingSpots.setValue(spots);
                        isLoading.setValue(false);

                        // Also update available count in selected parking area
                        updateAvailableSpotCount(parkingAreaId, spots);
                    }

                    @Override
                    public void onParkingSpotUpdated(ParkingSpot spot) {
                        // Single spot update, update the list
                        List<ParkingSpot> currentSpots = parkingSpots.getValue();
                        if (currentSpots != null) {
                            boolean spotFound = false;
                            for (int i = 0; i < currentSpots.size(); i++) {
                                if (currentSpots.get(i).getId().equals(spot.getId())) {
                                    currentSpots.set(i, spot);
                                    spotFound = true;
                                    break;
                                }
                            }

                            if (!spotFound) {
                                currentSpots.add(spot);
                            }

                            parkingSpots.setValue(currentSpots);

                            // Also update available count in selected parking area
                            updateAvailableSpotCount(parkingAreaId, currentSpots);
                        }
                    }

                    @Override
                    public void onError(String errorMsg) {
                        errorMessage.setValue("Error loading parking spots: " + errorMsg);
                        isLoading.setValue(false);
                    }
                });
    }

    /**
     * Select a parking area to view details
     */
    public void selectParkingArea(ParkingArea parkingArea) {
        selectedParkingArea.setValue(parkingArea);

        // Load spots for this parking area
        if (parkingArea != null) {
            loadParkingSpots(parkingArea.getId());
        }
    }

    /**
     * Toggle favorite status of a parking area
     */
    public void toggleFavorite(ParkingArea parkingArea) {
        if (!authManager.isUserLoggedIn()) {
            errorMessage.setValue("Please log in to save favorites");
            return;
        }

        String userId = authManager.getCurrentUser().getUid();
        boolean newStatus = !parkingArea.isFavorite();

        firestoreManager.updateFavoriteStatus(userId, parkingArea.getId(), newStatus,
                new FirestoreManager.FirestoreCallback() {
                    @Override
                    public void onSuccess() {
                        // Update the parking area in our list
                        parkingArea.setFavorite(newStatus);
                        updateParkingAreaInList(parkingArea);

                        // If this is the selected parking area, update it too
                        if (selectedParkingArea.getValue() != null &&
                                selectedParkingArea.getValue().getId().equals(parkingArea.getId())) {
                            selectedParkingArea.setValue(parkingArea);
                        }

                        // Update in local database
                        updateFavoriteInLocalDatabase(parkingArea.getId(), newStatus);
                    }

                    @Override
                    public void onFailure(String errorMsg) {
                        errorMessage.setValue("Error updating favorite: " + errorMsg);
                    }
                });
    }

    /**
     * Clear the selected parking area
     */
    public void clearSelectedParkingArea() {
        selectedParkingArea.setValue(null);

        // Remove any existing spots listener
        if (spotsListener != null && selectedParkingArea.getValue() != null) {
            realtimeDbManager.removeParkingSpotsListener(
                    selectedParkingArea.getValue().getId(), spotsListener);
            spotsListener = null;
        }
    }

    /**
     * Update the list with a modified parking area
     */
    private void updateParkingAreaInList(ParkingArea updatedArea) {
        List<ParkingArea> currentAreas = parkingAreas.getValue();
        if (currentAreas != null) {
            for (int i = 0; i < currentAreas.size(); i++) {
                if (currentAreas.get(i).getId().equals(updatedArea.getId())) {
                    currentAreas.set(i, updatedArea);
                    parkingAreas.setValue(currentAreas);
                    break;
                }
            }
        }
    }

    /**
     * Update available spot count in a parking area
     */
    private void updateAvailableSpotCount(String parkingAreaId, List<ParkingSpot> spots) {
        int availableCount = 0;
        for (ParkingSpot spot : spots) {
            if (spot.isAvailable()) {
                availableCount++;
            }
        }

        // Update the selected parking area if it matches
        ParkingArea selectedArea = selectedParkingArea.getValue();
        if (selectedArea != null && selectedArea.getId().equals(parkingAreaId)) {
            selectedArea.setAvailableSpots(availableCount);
            selectedParkingArea.setValue(selectedArea);
        }

        // Update in the list of all parking areas
        List<ParkingArea> currentAreas = parkingAreas.getValue();
        if (currentAreas != null) {
            for (ParkingArea area : currentAreas) {
                if (area.getId().equals(parkingAreaId)) {
                    area.setAvailableSpots(availableCount);
                    break;
                }
            }
            parkingAreas.setValue(currentAreas);
        }

        // Update in local database
        updateAvailableSpotsInLocalDatabase(parkingAreaId, availableCount);
    }

    /**
     * Load parking areas from local database
     */
    private void loadFromLocalDatabase(double latitude, double longitude) {
        databaseExecutor.execute(() -> {
            try {
                // Calculate squared radius for faster query (avoiding square root calculations)
                double radiusSquared = Constants.Location.DEFAULT_SEARCH_RADIUS * Constants.Location.DEFAULT_SEARCH_RADIUS;

                // Get nearby parking areas from local database
                List<ParkingAreaEntity> parkingAreaEntities =
                        parkingAreaDao.getNearbyParkingAreasSync(latitude, longitude, radiusSquared);

                if (parkingAreaEntities != null && !parkingAreaEntities.isEmpty()) {
                    // Convert entities to model objects
                    List<ParkingArea> localParkingAreas = new ArrayList<>();
                    for (ParkingAreaEntity entity : parkingAreaEntities) {
                        ParkingArea area = convertEntityToModel(entity);
                        localParkingAreas.add(area);
                    }

                    // Post to main thread
                    parkingAreas.postValue(localParkingAreas);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error loading from local database", e);
            }
        });
    }

    /**
     * Save parking areas to local database
     */
    private void saveToLocalDatabase(List<ParkingArea> areas) {
        databaseExecutor.execute(() -> {
            try {
                List<ParkingAreaEntity> entities = new ArrayList<>();
                for (ParkingArea area : areas) {
                    ParkingAreaEntity entity = convertModelToEntity(area);
                    entities.add(entity);
                }

                // Save entities to database
                parkingAreaDao.insertAll(entities);
            } catch (Exception e) {
                Log.e(TAG, "Error saving to local database", e);
            }
        });
    }

    /**
     * Update favorite status in local database
     */
    private void updateFavoriteInLocalDatabase(String parkingAreaId, boolean isFavorite) {
        databaseExecutor.execute(() -> {
            try {
                parkingAreaDao.updateFavoriteStatus(parkingAreaId, isFavorite);
            } catch (Exception e) {
                Log.e(TAG, "Error updating favorite in local database", e);
            }
        });
    }

    /**
     * Update available spots count in local database
     */
    private void updateAvailableSpotsInLocalDatabase(String parkingAreaId, int availableSpots) {
        databaseExecutor.execute(() -> {
            try {
                parkingAreaDao.updateAvailableSpots(parkingAreaId, availableSpots);
            } catch (Exception e) {
                Log.e(TAG, "Error updating available spots in local database", e);
            }
        });
    }

    /**
     * Update favorites status based on user's saved favorites
     */
    private void updateFavoritesStatus(List<ParkingArea> areas) {
        String userId = authManager.getCurrentUser().getUid();

        firestoreManager.getUserFavorites(userId, new FirestoreManager.GetFavoritesCallback() {
            @Override
            public void onSuccess(List<String> favoriteIds) {
                if (favoriteIds != null && !favoriteIds.isEmpty()) {
                    List<ParkingArea> updatedAreas = new ArrayList<>(areas);
                    boolean changed = false;

                    for (ParkingArea area : updatedAreas) {
                        boolean isFavorite = favoriteIds.contains(area.getId());
                        if (area.isFavorite() != isFavorite) {
                            area.setFavorite(isFavorite);
                            changed = true;
                        }
                    }

                    if (changed) {
                        parkingAreas.setValue(updatedAreas);

                        // Update in local database
                        saveToLocalDatabase(updatedAreas);
                    }
                }
            }

            @Override
            public void onFailure(String errorMsg) {
                Log.e(TAG, "Error getting user favorites: " + errorMsg);
            }
        });
    }

    /**
     * Convert ParkingArea model to ParkingAreaEntity
     */
    private ParkingAreaEntity convertModelToEntity(ParkingArea model) {
        return new ParkingAreaEntity(
                model.getId(),
                model.getName(),
                model.getAddress(),
                model.getLatitude(),
                model.getLongitude(),
                model.getTotalSpots(),
                model.getAvailableSpots(),
                model.getImageUrl(),
                model.getHourlyRate(),
                model.getOperatingHours(),
                model.isHasCoveredParking(),
                model.isHasDisabledAccess(),
                model.isHasElectricCharging(),
                model.getRating(),
                model.getNumberOfRatings()
        );
    }

    /**
     * Convert ParkingAreaEntity to ParkingArea model
     */
    private ParkingArea convertEntityToModel(ParkingAreaEntity entity) {
        ParkingArea model = new ParkingArea();
        model.setId(entity.getId());
        model.setName(entity.getName());
        model.setAddress(entity.getAddress());
        model.setLatitude(entity.getLatitude());
        model.setLongitude(entity.getLongitude());
        model.setTotalSpots(entity.getTotalSpots());
        model.setAvailableSpots(entity.getAvailableSpots());
        model.setImageUrl(entity.getImageUrl());
        model.setHourlyRate(entity.getHourlyRate());
        model.setOperatingHours(entity.getOperatingHours());
        model.setHasCoveredParking(entity.isHasCoveredParking());
        model.setHasDisabledAccess(entity.isHasDisabledAccess());
        model.setHasElectricCharging(entity.isHasElectricCharging());
        model.setRating(entity.getRating());
        model.setNumberOfRatings(entity.getNumberOfRatings());
        model.setFavorite(entity.isFavorite());
        return model;
    }

    /**
     * Clean up resources when ViewModel is cleared
     */
    @Override
    protected void onCleared() {
        super.onCleared();

        // Remove any active listeners
        if (spotsListener != null && selectedParkingArea.getValue() != null) {
            realtimeDbManager.removeParkingSpotsListener(
                    selectedParkingArea.getValue().getId(), spotsListener);
            spotsListener = null;
        }

        // Shut down executor
        databaseExecutor.shutdown();
    }
}