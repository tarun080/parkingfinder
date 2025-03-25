package com.example.parkingfinder.firebase;

import androidx.annotation.NonNull;

import com.example.parkingfinder.models.ParkingArea;
import com.example.parkingfinder.models.ParkingSpot;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manager class for Firebase Realtime Database operations.
 * Handles real-time updates of parking spots availability.
 */
public class RealtimeDbManager {
    private static RealtimeDbManager instance;
    private FirebaseDatabase database;

    // Database references
    private DatabaseReference parkingSpotsRef;

    // Callback interfaces
    public interface RealtimeCallback {
        void onSuccess();
        void onFailure(String errorMessage);
    }

    public interface GetParkingSpotsCallback {
        void onSuccess(List<ParkingSpot> parkingSpots);
        void onFailure(String errorMessage);
    }

    public interface ParkingSpotsListener {
        void onParkingSpotsUpdated(List<ParkingSpot> parkingSpots);
        void onParkingSpotUpdated(ParkingSpot parkingSpot);
        void onError(String errorMessage);
    }

    private RealtimeDbManager() {
        database = FirebaseDatabase.getInstance();
        parkingSpotsRef = database.getReference("parking_spots");
    }

    public static synchronized RealtimeDbManager getInstance() {
        if (instance == null) {
            instance = new RealtimeDbManager();
        }
        return instance;
    }

    /**
     * Initialize or update all parking spots for a parking area.
     * This should be called when a new parking area is created or when
     * the layout of a parking area changes.
     */
    public void initializeParkingSpots(String parkingAreaId, List<ParkingSpot> spots,
                                       final RealtimeCallback callback) {
        Map<String, Object> spotsMap = new HashMap<>();

        for (ParkingSpot spot : spots) {
            // Ensure spot has the correct parkingAreaId
            spot.setParkingAreaId(parkingAreaId);

            // Convert the spot to a map for database storage
            Map<String, Object> spotMap = new HashMap<>();
            spotMap.put("id", spot.getId());
            spotMap.put("parkingAreaId", spot.getParkingAreaId());
            spotMap.put("spotNumber", spot.getSpotNumber());
            spotMap.put("floor", spot.getFloor());
            spotMap.put("section", spot.getSection());
            spotMap.put("available", spot.isAvailable());
            spotMap.put("isReserved", spot.isReserved());
            spotMap.put("isHandicapped", spot.isHandicapped());
            spotMap.put("isElectricCharging", spot.isElectricCharging());
            spotMap.put("positionX", spot.getPositionX());
            spotMap.put("positionY", spot.getPositionY());
            spotMap.put("type", spot.getType());

            // Add to the spots map
            spotsMap.put(spot.getId(), spotMap);
        }

        // Save all spots under the parking area node
        DatabaseReference parkingAreaSpotsRef = parkingSpotsRef.child(parkingAreaId);
        parkingAreaSpotsRef.setValue(spotsMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        callback.onSuccess();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onFailure(e.getMessage());
                    }
                });
    }

    /**
     * Update the availability status of a specific parking spot.
     * This is used when a booking is made or canceled.
     */
    public void updateParkingSpotAvailability(String parkingAreaId, String spotId,
                                              boolean isAvailable, final RealtimeCallback callback) {
        DatabaseReference spotRef = parkingSpotsRef.child(parkingAreaId).child(spotId);

        Map<String, Object> updates = new HashMap<>();
        updates.put("available", isAvailable);

        spotRef.updateChildren(updates)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        callback.onSuccess();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onFailure(e.getMessage());
                    }
                });
    }

    /**
     * Get all parking spots for a specific parking area.
     * This method makes a one-time query, not a real-time listener.
     */
    public void getParkingSpots(String parkingAreaId, final GetParkingSpotsCallback callback) {
        DatabaseReference parkingAreaSpotsRef = parkingSpotsRef.child(parkingAreaId);

        parkingAreaSpotsRef.get()
                .addOnSuccessListener(dataSnapshot -> {
                    List<ParkingSpot> spots = new ArrayList<>();
                    for (DataSnapshot spotSnapshot : dataSnapshot.getChildren()) {
                        ParkingSpot spot = spotSnapshot.getValue(ParkingSpot.class);
                        if (spot != null) {
                            spots.add(spot);
                        }
                    }
                    callback.onSuccess(spots);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    /**
     * Set up a real-time listener for changes to parking spots in a specific area.
     * This is used to get live updates of spot availability.
     * Returns the ValueEventListener that can be used to remove the listener later.
     */
    public ValueEventListener addParkingSpotsListener(String parkingAreaId, final ParkingSpotsListener listener) {
        DatabaseReference parkingAreaSpotsRef = parkingSpotsRef.child(parkingAreaId);

        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<ParkingSpot> spots = new ArrayList<>();
                for (DataSnapshot spotSnapshot : dataSnapshot.getChildren()) {
                    ParkingSpot spot = spotSnapshot.getValue(ParkingSpot.class);
                    if (spot != null) {
                        spots.add(spot);
                    }
                }
                listener.onParkingSpotsUpdated(spots);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                listener.onError(databaseError.getMessage());
            }
        };

        parkingAreaSpotsRef.addValueEventListener(valueEventListener);
        return valueEventListener;
    }

    /**
     * Set up a real-time listener for changes to a specific parking spot.
     * This is used to get live updates of a single spot's availability.
     * Returns the ValueEventListener that can be used to remove the listener later.
     */
    public ValueEventListener addParkingSpotListener(String parkingAreaId, String spotId,
                                                     final ParkingSpotsListener listener) {
        DatabaseReference spotRef = parkingSpotsRef.child(parkingAreaId).child(spotId);

        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ParkingSpot spot = dataSnapshot.getValue(ParkingSpot.class);
                if (spot != null) {
                    listener.onParkingSpotUpdated(spot);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                listener.onError(databaseError.getMessage());
            }
        };

        spotRef.addValueEventListener(valueEventListener);
        return valueEventListener;
    }

    /**
     * Remove a real-time listener for parking spots updates.
     */
    public void removeParkingSpotsListener(String parkingAreaId, ValueEventListener listener) {
        if (listener != null) {
            DatabaseReference parkingAreaSpotsRef = parkingSpotsRef.child(parkingAreaId);
            parkingAreaSpotsRef.removeEventListener(listener);
        }
    }

    /**
     * Remove a real-time listener for a specific parking spot updates.
     */
    public void removeParkingSpotListener(String parkingAreaId, String spotId, ValueEventListener listener) {
        if (listener != null) {
            DatabaseReference spotRef = parkingSpotsRef.child(parkingAreaId).child(spotId);
            spotRef.removeEventListener(listener);
        }
    }

    /**
     * Update the total and available spots count in the parking area.
     * This should be called whenever the availability of a spot changes.
     */
    public void updateParkingAreaSpotCounts(String parkingAreaId, int totalSpots, int availableSpots,
                                            final RealtimeCallback callback) {
        DatabaseReference parkingAreaRef = database.getReference("parking_areas").child(parkingAreaId);

        Map<String, Object> updates = new HashMap<>();
        updates.put("totalSpots", totalSpots);
        updates.put("availableSpots", availableSpots);

        parkingAreaRef.updateChildren(updates)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        callback.onSuccess();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onFailure(e.getMessage());
                    }
                });
    }

    /**
     * Set up a real-time listener for changes to parking areas.
     * This is used to get live updates of all parking areas.
     * Returns the ValueEventListener that can be used to remove the listener later.
     */
    public ValueEventListener addParkingAreasListener(final ParkingAreaCallback listener) {
        DatabaseReference parkingAreasRef = database.getReference("parking_areas");

        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<ParkingArea> parkingAreas = new ArrayList<>();
                for (DataSnapshot areaSnapshot : dataSnapshot.getChildren()) {
                    ParkingArea area = areaSnapshot.getValue(ParkingArea.class);
                    if (area != null) {
                        area.setId(areaSnapshot.getKey());
                        parkingAreas.add(area);
                    }
                }
                listener.onParkingAreasUpdated(parkingAreas);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                listener.onError(databaseError.getMessage());
            }
        };

        parkingAreasRef.addValueEventListener(valueEventListener);
        return valueEventListener;
    }

    /**
     * Remove a real-time listener for parking areas updates.
     */
    public void removeParkingAreasListener(ValueEventListener listener) {
        if (listener != null) {
            DatabaseReference parkingAreasRef = database.getReference("parking_areas");
            parkingAreasRef.removeEventListener(listener);
        }
    }

    /**
     * Interface for parking areas updates.
     */
    public interface ParkingAreaCallback {
        void onParkingAreasUpdated(List<ParkingArea> parkingAreas);
        void onError(String errorMessage);
    }

    /**
     * Update the entire parking spot data.
     */
    public void updateParkingSpot(ParkingSpot spot, final RealtimeCallback callback) {
        DatabaseReference spotRef = parkingSpotsRef.child(spot.getParkingAreaId()).child(spot.getId());

        spotRef.setValue(spot)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        callback.onSuccess();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onFailure(e.getMessage());
                    }
                });
    }

    /**
     * Delete a parking spot.
     */
    public void deleteParkingSpot(String parkingAreaId, String spotId, final RealtimeCallback callback) {
        DatabaseReference spotRef = parkingSpotsRef.child(parkingAreaId).child(spotId);

        spotRef.removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        callback.onSuccess();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onFailure(e.getMessage());
                    }
                });
    }

    /**
     * Add a new parking spot.
     */
    public void addParkingSpot(ParkingSpot spot, final RealtimeCallback callback) {
        DatabaseReference spotsRef = parkingSpotsRef.child(spot.getParkingAreaId());
        DatabaseReference newSpotRef;

        if (spot.getId() != null && !spot.getId().isEmpty()) {
            // Use existing ID
            newSpotRef = spotsRef.child(spot.getId());
        } else {
            // Generate new ID
            newSpotRef = spotsRef.push();
            spot.setId(newSpotRef.getKey());
        }

        newSpotRef.setValue(spot)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        callback.onSuccess();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onFailure(e.getMessage());
                    }
                });
    }
}