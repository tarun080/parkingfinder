package com.example.parkingfinder.models;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String uid;
    private String name;
    private String email;
    private String phoneNumber;
    private String profileImageUrl;
    private List<String> favoriteLocations;
    private List<String> bookingHistory;

    public User() {
        // Required empty constructor for Firestore
        this.favoriteLocations = new ArrayList<>();
        this.bookingHistory = new ArrayList<>();
    }

    public User(String uid, String name, String email, String phoneNumber, String profileImageUrl) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.profileImageUrl = profileImageUrl;
        this.favoriteLocations = new ArrayList<>();
        this.bookingHistory = new ArrayList<>();
    }

    // Getters and Setters
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public List<String> getFavoriteLocations() {
        return favoriteLocations;
    }

    public void setFavoriteLocations(List<String> favoriteLocations) {
        this.favoriteLocations = favoriteLocations;
    }

    public void addFavoriteLocation(String locationId) {
        if (this.favoriteLocations == null) {
            this.favoriteLocations = new ArrayList<>();
        }
        this.favoriteLocations.add(locationId);
    }

    public void removeFavoriteLocation(String locationId) {
        if (this.favoriteLocations != null) {
            this.favoriteLocations.remove(locationId);
        }
    }

    public List<String> getBookingHistory() {
        return bookingHistory;
    }

    public void setBookingHistory(List<String> bookingHistory) {
        this.bookingHistory = bookingHistory;
    }

    public void addBookingToHistory(String bookingId) {
        if (this.bookingHistory == null) {
            this.bookingHistory = new ArrayList<>();
        }
        this.bookingHistory.add(bookingId);
    }
}