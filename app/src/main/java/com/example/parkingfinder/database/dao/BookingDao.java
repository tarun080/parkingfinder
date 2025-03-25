package com.example.parkingfinder.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.parkingfinder.database.entities.BookingEntity;

import java.util.List;

@Dao
public interface BookingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(BookingEntity booking);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<BookingEntity> bookings);

    @Update
    void update(BookingEntity booking);

    @Delete
    void delete(BookingEntity booking);

    @Query("SELECT * FROM bookings WHERE id = :id")
    LiveData<BookingEntity> getBookingById(String id);

    @Query("SELECT * FROM bookings WHERE id = :id")
    BookingEntity getBookingByIdSync(String id);

    @Query("SELECT * FROM bookings WHERE userId = :userId ORDER BY createdAt DESC")
    LiveData<List<BookingEntity>> getBookingsByUserId(String userId);

    @Query("SELECT * FROM bookings WHERE userId = :userId AND status = :status ORDER BY createdAt DESC")
    LiveData<List<BookingEntity>> getBookingsByUserIdAndStatus(String userId, String status);

    @Query("SELECT * FROM bookings WHERE userId = :userId AND startTime >= :currentTime ORDER BY startTime ASC")
    LiveData<List<BookingEntity>> getUpcomingBookings(String userId, long currentTime);

    @Query("SELECT * FROM bookings WHERE userId = :userId AND startTime <= :currentTime AND endTime >= :currentTime")
    LiveData<List<BookingEntity>> getCurrentBookings(String userId, long currentTime);

    @Query("SELECT * FROM bookings WHERE userId = :userId AND endTime < :currentTime ORDER BY endTime DESC")
    LiveData<List<BookingEntity>> getPastBookings(String userId, long currentTime);

    @Query("SELECT * FROM bookings WHERE isSynced = 0")
    List<BookingEntity> getUnsyncedBookings();

    @Query("UPDATE bookings SET isSynced = 1 WHERE id = :id")
    void markAsSynced(String id);

    @Query("UPDATE bookings SET status = :status WHERE id = :id")
    void updateStatus(String id, String status);

    @Query("UPDATE bookings SET endTime = :newEndTime, totalCost = :newTotalCost WHERE id = :id")
    void updateBookingEndTime(String id, long newEndTime, double newTotalCost);

    @Query("UPDATE bookings SET isPaid = :isPaid WHERE id = :id")
    void updatePaymentStatus(String id, boolean isPaid);

    @Query("UPDATE bookings SET paymentMethod = :paymentMethod, paymentId = :paymentId, isPaid = 1 WHERE id = :id")
    void updatePaymentInfo(String id, String paymentMethod, String paymentId);

    @Query("DELETE FROM bookings WHERE id = :id")
    void deleteById(String id);

    @Query("DELETE FROM bookings")
    void deleteAll();

    @Query("DELETE FROM bookings WHERE userId = :userId")
    void deleteAllByUserId(String userId);

    @Query("SELECT COUNT(*) FROM bookings WHERE userId = :userId AND status IN ('PENDING', 'CONFIRMED', 'ACTIVE')")
    int countActiveBookings(String userId);

    @Query("SELECT * FROM bookings WHERE endTime < :currentTime AND status = 'ACTIVE'")
    List<BookingEntity> getExpiredBookings(long currentTime);

    @Query("SELECT COUNT(*) FROM bookings WHERE parkingAreaId = :parkingAreaId AND userId = :userId AND status NOT IN ('CANCELLED', 'COMPLETED') AND endTime > :currentTime")
    int countActiveBookingsForParkingArea(String parkingAreaId, String userId, long currentTime);

    @Query("SELECT * FROM bookings WHERE parkingAreaId = :parkingAreaId AND parkingSpotId = :spotId AND status IN ('PENDING', 'CONFIRMED', 'ACTIVE') AND endTime > :currentTime")
    List<BookingEntity> getActiveBookingsForParkingSpot(String parkingAreaId, String spotId, long currentTime);

    @Query("SELECT * FROM bookings WHERE userId = :userId ORDER BY createdAt DESC LIMIT :limit")
    LiveData<List<BookingEntity>> getRecentBookings(String userId, int limit);

    List<BookingEntity> getActiveBookingsSync(String userId);

//    this is the first commit
}