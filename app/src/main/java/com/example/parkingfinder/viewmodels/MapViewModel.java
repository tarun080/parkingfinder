package com.example.parkingfinder.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.parkingfinder.firebase.FirestoreManager;
import com.example.parkingfinder.models.ParkingArea;

import java.util.List;

public class MapViewModel extends ViewModel {
    private FirestoreManager firestoreManager;
    private MutableLiveData<List<ParkingArea>> parkingAreas = new MutableLiveData<>();
    private MutableLiveData<ParkingArea> selectedParkingArea = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public MapViewModel() {
        firestoreManager = FirestoreManager.getInstance();
    }

    public LiveData<List<ParkingArea>> getParkingAreas() {
        return parkingAreas;
    }

    public LiveData<ParkingArea> getSelectedParkingArea() {
        return selectedParkingArea;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void loadNearbyParkingAreas(double latitude, double longitude, double radiusInKm) {
        isLoading.setValue(true);

        firestoreManager.getNearbyParkingAreas(latitude, longitude, radiusInKm,
                new FirestoreManager.GetParkingAreasCallback() {
                    @Override
                    public void onSuccess(List<ParkingArea> areas) {
                        parkingAreas.setValue(areas);
                        isLoading.setValue(false);
                    }

                    @Override
                    public void onFailure(String message) {
                        errorMessage.setValue(message);
                        isLoading.setValue(false);
                    }
                });
    }

    public void selectParkingArea(ParkingArea parkingArea) {
        selectedParkingArea.setValue(parkingArea);
    }

    public void refreshParkingAreaDetails(String parkingAreaId) {
        // Implement if needed to refresh a specific parking area's details
    }

    public void clearSelectedParkingArea() {
        selectedParkingArea.setValue(null);
    }
}