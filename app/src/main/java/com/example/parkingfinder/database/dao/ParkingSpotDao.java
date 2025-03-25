package com.example.parkingfinder.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.parkingfinder.database.entities.ParkingSpotEntity;

import java.util.List;

@Dao
public interface ParkingSpotDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ParkingSpotEntity parkingSpot);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<ParkingSpotEntity> parkingSpots);

    @Update
    void update(ParkingSpotEntity parkingSpot);

    @Delete
    void delete(ParkingSpotEntity parkingSpot);

    @Query("SELECT * FROM parking_spots WHERE id = :id")
    LiveData<ParkingSpotEntity> getParkingSpotById(String id);

    @Query("SELECT * FROM parking_spots WHERE id = :id")
    ParkingSpotEntity getParkingSpotByIdSync(String id);

    @Query("SELECT * FROM parking_spots WHERE parkingAreaId = :parkingAreaId")
    LiveData<List<ParkingSpotEntity>> getParkingSpotsByParkingAreaId(String parkingAreaId);

    @Query("SELECT * FROM parking_spots WHERE parkingAreaId = :parkingAreaId AND available = 1")
    LiveData<List<ParkingSpotEntity>> getAvailableParkingSpotsByParkingAreaId(String parkingAreaId);

    @Query("UPDATE parking_spots SET available = :isAvailable WHERE id = :id")
    void updateAvailability(String id, boolean isAvailable);

    @Query("DELETE FROM parking_spots WHERE parkingAreaId = :parkingAreaId")
    void deleteByParkingAreaId(String parkingAreaId);

    @Query("DELETE FROM parking_spots")
    void deleteAll();

    @Query("DELETE FROM parking_spots WHERE lastUpdated < :timestamp")
    void deleteOld(long timestamp);
}