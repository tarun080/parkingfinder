package com.example.parkingfinder.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.parkingfinder.database.AppDatabase;
import com.example.parkingfinder.database.entities.BookingEntity;
import com.example.parkingfinder.firebase.FirestoreManager;
import com.example.parkingfinder.models.Booking;

import java.util.ArrayList;
import java.util.List;

public class BookingViewModel extends ViewModel {
    private FirestoreManager firestoreManager;
    private MutableLiveData<List<Booking>> bookings = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public BookingViewModel() {
        firestoreManager = FirestoreManager.getInstance();
        bookings.setValue(new ArrayList<>());
    }

    public LiveData<List<Booking>> getBookings() {
        return bookings;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void loadBookings(String userId) {
        isLoading.setValue(true);

        firestoreManager.getUserBookings(userId, new FirestoreManager.GetBookingsCallback() {
            @Override
            public void onSuccess(List<Booking> bookingList) {
                bookings.setValue(bookingList);
                isLoading.setValue(false);

                // Cache bookings in local database for offline access
                cacheBookingsInLocalDb(userId, bookingList);
            }

            @Override
            public void onFailure(String message) {
                errorMessage.setValue(message);
                isLoading.setValue(false);

                // Try to load from local database if online fetch fails
                loadBookingsFromLocalDb(userId);
            }
        });
    }

    private void cacheBookingsInLocalDb(String userId, List<Booking> bookingList) {
        // This method would convert Booking models to BookingEntity objects and store them in Room
        // This is just a skeleton - implementation depends on how you've set up your Room database

        // Example implementation:
        /*
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<BookingEntity> bookingEntities = new ArrayList<>();
            for (Booking booking : bookingList) {
                BookingEntity entity = new BookingEntity(
                    booking.getId(),
                    booking.getUserId(),
                    booking.getParkingAreaId(),
                    booking.getParkingSpotId(),
                    booking.getParkingAreaName(),
                    booking.getParkingSpotNumber(),
                    booking.getStartTime().getTime(),
                    booking.getEndTime().getTime(),
                    booking.getTotalCost(),
                    booking.getStatus()
                );
                entity.setVehicleRegistration(booking.getVehicleRegistration());
                entity.setPaymentMethod(booking.getPaymentMethod());
                entity.setPaymentId(booking.getPaymentId());
                entity.setPaid(booking.isPaid());
                entity.setConfirmationCode(booking.getConfirmationCode());
                entity.setCreatedAt(booking.getCreatedAt().getTime());
                entity.setSynced(true);

                bookingEntities.add(entity);
            }

            AppDatabase db = AppDatabase.getInstance();
            db.bookingDao().deleteAllByUserId(userId);
            db.bookingDao().insertAll(bookingEntities);
        });
        */
    }

    private void loadBookingsFromLocalDb(String userId) {
        // This method would load BookingEntity objects from Room and convert them to Booking models
        // This is just a skeleton - implementation depends on how you've set up your Room database

        // Example implementation:
        /*
        AppDatabase db = AppDatabase.getInstance();
        db.bookingDao().getBookingsByUserId(userId).observeForever(bookingEntities -> {
            if (bookingEntities != null && !bookingEntities.isEmpty()) {
                List<Booking> localBookings = new ArrayList<>();
                for (BookingEntity entity : bookingEntities) {
                    Booking booking = new Booking(
                        entity.getUserId(),
                        entity.getParkingAreaId(),
                        entity.getParkingSpotId(),
                        entity.getParkingAreaName(),
                        entity.getParkingSpotNumber(),
                        new Date(entity.getStartTime()),
                        new Date(entity.getEndTime()),
                        entity.getTotalCost()
                    );
                    booking.setId(entity.getId());
                    booking.setStatus(entity.getStatus());
                    booking.setVehicleRegistration(entity.getVehicleRegistration());
                    booking.setPaymentMethod(entity.getPaymentMethod());
                    booking.setPaymentId(entity.getPaymentId());
                    booking.setPaid(entity.isPaid());
                    booking.setConfirmationCode(entity.getConfirmationCode());
                    booking.setCreatedAt(new Date(entity.getCreatedAt()));

                    localBookings.add(booking);
                }

                bookings.postValue(localBookings);
            }
        });
        */
    }

    public void cancelBooking(String bookingId, String parkingAreaId, String parkingSpotId) {
        isLoading.setValue(true);

        firestoreManager.cancelBooking(bookingId, parkingAreaId, parkingSpotId,
                new FirestoreManager.FirestoreCallback() {
                    @Override
                    public void onSuccess() {
                        // Refresh bookings list
                        if (bookings.getValue() != null) {
                            List<Booking> currentBookings = new ArrayList<>(bookings.getValue());
                            for (Booking booking : currentBookings) {
                                if (booking.getId().equals(bookingId)) {
                                    booking.setStatus("CANCELLED");
                                    break;
                                }
                            }
                            bookings.setValue(currentBookings);
                        }
                        isLoading.setValue(false);
                    }

                    @Override
                    public void onFailure(String errorMsg) {
                        errorMessage.setValue(errorMsg);
                        isLoading.setValue(false);
                    }
                });
    }

    public void extendBooking(String bookingId, long newEndTime, double newTotalCost) {
        isLoading.setValue(true);

        // This implementation would depend on your FirestoreManager
        // Example:
        /*
        firestoreManager.extendBooking(bookingId, newEndTime, newTotalCost,
            new FirestoreManager.FirestoreCallback() {
                @Override
                public void onSuccess() {
                    // Refresh bookings list or update the specific booking
                    if (bookings.getValue() != null) {
                        List<Booking> currentBookings = new ArrayList<>(bookings.getValue());
                        for (Booking booking : currentBookings) {
                            if (booking.getId().equals(bookingId)) {
                                booking.setEndTime(new Date(newEndTime));
                                booking.setTotalCost(newTotalCost);
                                break;
                            }
                        }
                        bookings.setValue(currentBookings);
                    }
                    isLoading.setValue(false);
                }

                @Override
                public void onFailure(String errorMsg) {
                    errorMessage.setValue(errorMsg);
                    isLoading.setValue(false);
                }
            });
        */
    }
}