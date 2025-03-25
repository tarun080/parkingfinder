package com.example.parkingfinder.database.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "parking_areas")
public class ParkingAreaEntity {
    @PrimaryKey
    @NonNull
    private String id;
    private String name;
    private String address;
    private double latitude;
    private double longitude;
    private int totalSpots;
    private int availableSpots;
    private String imageUrl;
    private double hourlyRate;
    private String operatingHours;
    private boolean hasCoveredParking;
    private boolean hasDisabledAccess;
    private boolean hasElectricCharging;
    private float rating;
    private int numberOfRatings;
    private long lastUpdated;
    private boolean isFavorite;

    public ParkingAreaEntity(@NonNull String id, String name, String address, double latitude, double longitude,
                             int totalSpots, int availableSpots, String imageUrl, double hourlyRate,
                             String operatingHours, boolean hasCoveredParking, boolean hasDisabledAccess,
                             boolean hasElectricCharging, float rating, int numberOfRatings) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.totalSpots = totalSpots;
        this.availableSpots = availableSpots;
        this.imageUrl = imageUrl;
        this.hourlyRate = hourlyRate;
        this.operatingHours = operatingHours;
        this.hasCoveredParking = hasCoveredParking;
        this.hasDisabledAccess = hasDisabledAccess;
        this.hasElectricCharging = hasElectricCharging;
        this.rating = rating;
        this.numberOfRatings = numberOfRatings;
        this.lastUpdated = System.currentTimeMillis();
        this.isFavorite = false;
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public int getTotalSpots() {
        return totalSpots;
    }

    public void setTotalSpots(int totalSpots) {
        this.totalSpots = totalSpots;
    }

    public int getAvailableSpots() {
        return availableSpots;
    }

    public void setAvailableSpots(int availableSpots) {
        this.availableSpots = availableSpots;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public double getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(double hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    public String getOperatingHours() {
        return operatingHours;
    }

    public void setOperatingHours(String operatingHours) {
        this.operatingHours = operatingHours;
    }

    public boolean isHasCoveredParking() {
        return hasCoveredParking;
    }

    public void setHasCoveredParking(boolean hasCoveredParking) {
        this.hasCoveredParking = hasCoveredParking;
    }

    public boolean isHasDisabledAccess() {
        return hasDisabledAccess;
    }

    public void setHasDisabledAccess(boolean hasDisabledAccess) {
        this.hasDisabledAccess = hasDisabledAccess;
    }

    public boolean isHasElectricCharging() {
        return hasElectricCharging;
    }

    public void setHasElectricCharging(boolean hasElectricCharging) {
        this.hasElectricCharging = hasElectricCharging;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public int getNumberOfRatings() {
        return numberOfRatings;
    }

    public void setNumberOfRatings(int numberOfRatings) {
        this.numberOfRatings = numberOfRatings;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }
}