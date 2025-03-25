package com.example.parkingfinder.firebase;

import androidx.annotation.NonNull;

import com.example.parkingfinder.models.Booking;
import com.example.parkingfinder.models.ParkingArea;
import com.example.parkingfinder.models.ParkingSpot;
import com.example.parkingfinder.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import androidx.annotation.NonNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirestoreManager {
    private static FirestoreManager instance;
    private FirebaseFirestore db;

    // Collection names
    private static final String USERS_COLLECTION = "users";
    private static final String PARKING_AREAS_COLLECTION = "parking_areas";
    private static final String PARKING_SPOTS_COLLECTION = "parking_spots";
    private static final String BOOKINGS_COLLECTION = "bookings";

    public void deleteUserData(String uid, FirestoreCallback accountDeletedSuccessfully) {

    }

    public void updateFavoriteStatus(String uid, String id, boolean isFavorite, FirestoreCallback firestoreCallback) {

    }

    public void getUserFavorites(String userId, FirestoreManager.GetFavoritesCallback getFavoritesCallback) {

    }

    // Callback interfaces
    public interface FirestoreCallback {
        void onSuccess();
        void onFailure(String errorMessage);
    }

    public interface GetUserCallback {
        void onSuccess(User user);
        void onFailure(String errorMessage);
    }

    public interface GetParkingAreasCallback {
        void onSuccess(List<ParkingArea> parkingAreas);
        void onFailure(String errorMessage);
    }

    public interface GetParkingSpotsCallback {
        void onSuccess(List<ParkingSpot> parkingSpots);
        void onFailure(String errorMessage);
    }

    public interface GetBookingsCallback {
        void onSuccess(List<Booking> bookings);
        void onFailure(String errorMessage);
    }

    private FirestoreManager() {
        db = FirebaseFirestore.getInstance();
    }

    public static synchronized FirestoreManager getInstance() {
        if (instance == null) {
            instance = new FirestoreManager();
        }
        return instance;
    }

    // User operations
    public void createUserProfile(User user, final FirestoreCallback callback) {
        db.collection(USERS_COLLECTION).document(user.getUid())
                .set(user)
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

    public void getUserProfile(String userId, final GetUserCallback callback) {
        db.collection(USERS_COLLECTION).document(userId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                User user = document.toObject(User.class);
                                callback.onSuccess(user);
                            } else {
                                callback.onFailure("User profile not found");
                            }
                        } else {
                            callback.onFailure(task.getException().getMessage());
                        }
                    }
                });
    }

    public void updateUserProfile(User user, final FirestoreCallback callback) {
        db.collection(USERS_COLLECTION).document(user.getUid())
                .set(user)
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

    // Parking Area operations
    public void getNearbyParkingAreas(double latitude, double longitude, double radiusInKm,
                                      final GetParkingAreasCallback callback) {
        // In a real app, you would use Firestore's GeoPoint and geoquery capabilities
        // For simplicity, we'll just fetch all parking areas and filter them client-side
        db.collection(PARKING_AREAS_COLLECTION)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<ParkingArea> nearbyParkingAreas = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                ParkingArea parkingArea = document.toObject(ParkingArea.class);
                                parkingArea.setId(document.getId());

                                // Calculate distance and filter (simple implementation)
                                // In a real app, you would use a more sophisticated approach
                                double distance = calculateDistance(latitude, longitude,
                                        parkingArea.getLatitude(), parkingArea.getLongitude());
                                if (distance <= radiusInKm) {
                                    nearbyParkingAreas.add(parkingArea);
                                }
                            }
                            callback.onSuccess(nearbyParkingAreas);
                        } else {
                            callback.onFailure(task.getException().getMessage());
                        }
                    }
                });
    }

    public void getParkingSpots(String parkingAreaId, final GetParkingSpotsCallback callback) {
        db.collection(PARKING_AREAS_COLLECTION).document(parkingAreaId)
                .collection(PARKING_SPOTS_COLLECTION)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<ParkingSpot> spots = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                ParkingSpot spot = document.toObject(ParkingSpot.class);
                                spot.setId(document.getId());
                                spot.setParkingAreaId(parkingAreaId);
                                spots.add(spot);
                            }
                            callback.onSuccess(spots);
                        } else {
                            callback.onFailure(task.getException().getMessage());
                        }
                    }
                });
    }

    // Booking operations
    public void createBooking(Booking booking, final FirestoreCallback callback) {
        // Add booking to bookings collection
        db.collection(BOOKINGS_COLLECTION)
                .add(booking)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        String bookingId = documentReference.getId();

                        // Update parking spot status
                        updateParkingSpotStatus(booking.getParkingAreaId(), booking.getParkingSpotId(),
                                false, new FirestoreCallback() {
                                    @Override
                                    public void onSuccess() {
                                        // Add booking to user's history
                                        addBookingToUserHistory(booking.getUserId(), bookingId, callback);
                                    }

                                    @Override
                                    public void onFailure(String errorMessage) {
                                        callback.onFailure(errorMessage);
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onFailure(e.getMessage());
                    }
                });
    }

    public void getUserBookings(String userId, final GetBookingsCallback callback) {
        db.collection(BOOKINGS_COLLECTION)
                .whereEqualTo("userId", userId)
                .orderBy("startTime", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<Booking> bookings = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Booking booking = document.toObject(Booking.class);
                                booking.setId(document.getId());
                                bookings.add(booking);
                            }
                            callback.onSuccess(bookings);
                        } else {
                            callback.onFailure(task.getException().getMessage());
                        }
                    }
                });
    }

    private void addBookingToUserHistory(String userId, String bookingId, final FirestoreCallback callback) {
        DocumentReference userRef = db.collection(USERS_COLLECTION).document(userId);

        // Get current user data
        userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        User user = document.toObject(User.class);
                        user.addBookingToHistory(bookingId);

                        // Update user document
                        userRef.set(user)
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
                    } else {
                        callback.onFailure("User not found");
                    }
                } else {
                    callback.onFailure(task.getException().getMessage());
                }
            }
        });
    }

    private void updateParkingSpotStatus(String parkingAreaId, String spotId, boolean isAvailable, final FirestoreCallback callback) {
        DocumentReference spotRef = db.collection(PARKING_AREAS_COLLECTION)
                .document(parkingAreaId)
                .collection(PARKING_SPOTS_COLLECTION)
                .document(spotId);

        Map<String, Object> updates = new HashMap<>();
        updates.put("available", isAvailable);

        spotRef.update(updates)
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

    // Helper method to calculate distance between two points
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // Simple implementation of haversine formula
        final int R = 6371; // Radius of the earth in km

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c; // Distance in km
    }

    // Additional methods for parking area CRUD operations
    public void addParkingArea(ParkingArea parkingArea, final FirestoreCallback callback) {
        db.collection(PARKING_AREAS_COLLECTION)
                .add(parkingArea)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        parkingArea.setId(documentReference.getId());
                        documentReference.set(parkingArea)
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
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onFailure(e.getMessage());
                    }
                });
    }

    public void updateParkingArea(ParkingArea parkingArea, final FirestoreCallback callback) {
        db.collection(PARKING_AREAS_COLLECTION).document(parkingArea.getId())
                .set(parkingArea)
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

    public void addParkingSpot(ParkingSpot parkingSpot, final FirestoreCallback callback) {
        db.collection(PARKING_AREAS_COLLECTION).document(parkingSpot.getParkingAreaId())
                .collection(PARKING_SPOTS_COLLECTION)
                .add(parkingSpot)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        parkingSpot.setId(documentReference.getId());
                        documentReference.set(parkingSpot)
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
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onFailure(e.getMessage());
                    }
                });
    }

    public void updateParkingSpot(ParkingSpot parkingSpot, final FirestoreCallback callback) {
        db.collection(PARKING_AREAS_COLLECTION).document(parkingSpot.getParkingAreaId())
                .collection(PARKING_SPOTS_COLLECTION).document(parkingSpot.getId())
                .set(parkingSpot)
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

    public void deleteParkingArea(String parkingAreaId, final FirestoreCallback callback) {
        // Note: This will delete all parking spots within this area as well due to Firestore rules
        db.collection(PARKING_AREAS_COLLECTION).document(parkingAreaId)
                .delete()
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

    public void deleteParkingSpot(String parkingAreaId, String spotId, final FirestoreCallback callback) {
        db.collection(PARKING_AREAS_COLLECTION).document(parkingAreaId)
                .collection(PARKING_SPOTS_COLLECTION).document(spotId)
                .delete()
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

    public void updateBookingStatus(String bookingId, String status, final FirestoreCallback callback) {
        db.collection(BOOKINGS_COLLECTION).document(bookingId)
                .update("status", status)
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

    public void cancelBooking(String bookingId, String parkingAreaId, String parkingSpotId, final FirestoreCallback callback) {
        // Update booking status to CANCELLED
        db.collection(BOOKINGS_COLLECTION).document(bookingId)
                .update("status", "CANCELLED")
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Make the parking spot available again
                        updateParkingSpotStatus(parkingAreaId, parkingSpotId, true, callback);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onFailure(e.getMessage());
                    }
                });
    }

    public static class GetFavoritesCallback {
    }
}