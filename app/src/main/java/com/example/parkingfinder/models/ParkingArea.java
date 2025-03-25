package com.example.parkingfinder.models;

import java.util.List;

public class ParkingArea {
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
    private List<String> amenities;
    private boolean hasCoveredParking;
    private boolean hasDisabledAccess;
    private boolean hasElectricCharging;
    private float rating;
    private int numberOfRatings;
    private boolean favorite;

    public ParkingArea() {
        // Required empty constructor for Firestore
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
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

    public List<String> getAmenities() {
        return amenities;
    }

    public void setAmenities(List<String> amenities) {
        this.amenities = amenities;
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

    // Helper method to calculate distance from a location
    public double distanceFrom(double lat, double lng) {
        final int R = 6371; // Radius of the earth in km

        double latDistance = Math.toRadians(this.latitude - lat);
        double lonDistance = Math.toRadians(this.longitude - lng);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat)) * Math.cos(Math.toRadians(this.latitude))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c; // Distance in km
    }


    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    public boolean isFavorite() {
        return favorite;
    }
}