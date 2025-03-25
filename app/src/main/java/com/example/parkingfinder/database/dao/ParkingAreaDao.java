package com.example.parkingfinder.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.parkingfinder.database.entities.ParkingAreaEntity;

import java.util.List;

@Dao
public interface ParkingAreaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ParkingAreaEntity parkingArea);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<ParkingAreaEntity> parkingAreas);

    @Update
    void update(ParkingAreaEntity parkingArea);

    @Delete
    void delete(ParkingAreaEntity parkingArea);

    @Query("SELECT * FROM parking_areas WHERE id = :id")
    LiveData<ParkingAreaEntity> getParkingAreaById(String id);

    @Query("SELECT * FROM parking_areas WHERE id = :id")
    ParkingAreaEntity getParkingAreaByIdSync(String id);

    @Query("SELECT * FROM parking_areas")
    LiveData<List<ParkingAreaEntity>> getAllParkingAreas();

    @Query("SELECT * FROM parking_areas WHERE isFavorite = 1")
    LiveData<List<ParkingAreaEntity>> getFavoriteParkingAreas();

    @Query("SELECT * FROM parking_areas ORDER BY " +
            "((latitude - :userLat) * (latitude - :userLat) + " +
            "(longitude - :userLng) * (longitude - :userLng)) ASC")
    LiveData<List<ParkingAreaEntity>> getNearbyParkingAreas(double userLat, double userLng);

    @Query("SELECT * FROM parking_areas WHERE " +
            "((latitude - :userLat) * (latitude - :userLat) + " +
            "(longitude - :userLng) * (longitude - :userLng)) <= :radiusSquared")
    LiveData<List<ParkingAreaEntity>> getParkingAreasWithinRadius(double userLat, double userLng, double radiusSquared);

    @Query("SELECT * FROM parking_areas WHERE " +
            "availableSpots > 0 AND " +
            "hourlyRate <= :maxPrice AND " +
            "((latitude - :userLat) * (latitude - :userLat) + " +
            "(longitude - :userLng) * (longitude - :userLng)) <= :radiusSquared")
    LiveData<List<ParkingAreaEntity>> getFilteredParkingAreas(
            double userLat, double userLng, double radiusSquared, double maxPrice);

    @Query("SELECT * FROM parking_areas WHERE " +
            "availableSpots > 0 AND " +
            "hourlyRate <= :maxPrice AND " +
            "hasElectricCharging = :hasEV AND " +
            "hasDisabledAccess = :hasDisabled AND " +
            "((latitude - :userLat) * (latitude - :userLat) + " +
            "(longitude - :userLng) * (longitude - :userLng)) <= :radiusSquared")
    LiveData<List<ParkingAreaEntity>> getFullyFilteredParkingAreas(
            double userLat, double userLng, double radiusSquared,
            double maxPrice, boolean hasEV, boolean hasDisabled);

    @Query("SELECT * FROM parking_areas WHERE name LIKE '%' || :query || '%' OR address LIKE '%' || :query || '%'")
    LiveData<List<ParkingAreaEntity>> searchParkingAreas(String query);

    @Query("UPDATE parking_areas SET isFavorite = :isFavorite WHERE id = :id")
    void updateFavoriteStatus(String id, boolean isFavorite);

    @Query("UPDATE parking_areas SET availableSpots = :availableSpots WHERE id = :id")
    void updateAvailableSpots(String id, int availableSpots);

    @Query("UPDATE parking_areas SET rating = :rating, numberOfRatings = numberOfRatings + 1 WHERE id = :id")
    void updateRating(String id, float rating);

    @Query("UPDATE parking_areas SET totalSpots = :totalSpots WHERE id = :id")
    void updateTotalSpots(String id, int totalSpots);

    @Query("UPDATE parking_areas SET imageUrl = :imageUrl WHERE id = :id")
    void updateImageUrl(String id, String imageUrl);

    @Query("UPDATE parking_areas SET hourlyRate = :hourlyRate WHERE id = :id")
    void updateHourlyRate(String id, double hourlyRate);

    @Query("UPDATE parking_areas SET operatingHours = :operatingHours WHERE id = :id")
    void updateOperatingHours(String id, String operatingHours);

    @Query("DELETE FROM parking_areas")
    void deleteAll();

    @Query("DELETE FROM parking_areas WHERE lastUpdated < :timestamp")
    void deleteOld(long timestamp);

    @Query("DELETE FROM parking_areas WHERE id = :id")
    void deleteById(String id);

    List<ParkingAreaEntity> getNearbyParkingAreasSync(double latitude, double longitude, double radiusSquared);
}