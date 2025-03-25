package com.example.parkingfinder.models;

import java.util.Date;

public class Booking {
    private String id;
    private String userId;
    private String parkingAreaId;
    private String parkingSpotId;
    private String parkingAreaName;
    private String parkingSpotNumber;
    private Date startTime;
    private Date endTime;
    private double totalCost;
    private String paymentMethod;
    private String paymentId;
    private String status; // PENDING, CONFIRMED, ACTIVE, COMPLETED, CANCELLED
    private Date createdAt;
    private String vehicleRegistration;
    private boolean isPaid;
    private String confirmationCode;

    public Booking() {
        // Required empty constructor for Firestore
        this.createdAt = new Date();
    }

    public Booking(String userId, String parkingAreaId, String parkingSpotId,
                   String parkingAreaName, String parkingSpotNumber,
                   Date startTime, Date endTime, double totalCost) {
        this.userId = userId;
        this.parkingAreaId = parkingAreaId;
        this.parkingSpotId = parkingSpotId;
        this.parkingAreaName = parkingAreaName;
        this.parkingSpotNumber = parkingSpotNumber;
        this.startTime = startTime;
        this.endTime = endTime;
        this.totalCost = totalCost;
        this.status = "PENDING";
        this.createdAt = new Date();
        this.isPaid = false;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getParkingAreaId() {
        return parkingAreaId;
    }

    public void setParkingAreaId(String parkingAreaId) {
        this.parkingAreaId = parkingAreaId;
    }

    public String getParkingSpotId() {
        return parkingSpotId;
    }

    public void setParkingSpotId(String parkingSpotId) {
        this.parkingSpotId = parkingSpotId;
    }

    public String getParkingAreaName() {
        return parkingAreaName;
    }

    public void setParkingAreaName(String parkingAreaName) {
        this.parkingAreaName = parkingAreaName;
    }

    public String getParkingSpotNumber() {
        return parkingSpotNumber;
    }

    public void setParkingSpotNumber(String parkingSpotNumber) {
        this.parkingSpotNumber = parkingSpotNumber;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public double getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(double totalCost) {
        this.totalCost = totalCost;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getVehicleRegistration() {
        return vehicleRegistration;
    }

    public void setVehicleRegistration(String vehicleRegistration) {
        this.vehicleRegistration = vehicleRegistration;
    }

    public boolean isPaid() {
        return isPaid;
    }

    public void setPaid(boolean paid) {
        isPaid = paid;
    }

    public String getConfirmationCode() {
        return confirmationCode;
    }

    public void setConfirmationCode(String confirmationCode) {
        this.confirmationCode = confirmationCode;
    }

    // Helper method to calculate duration in hours
    public double getDurationInHours() {
        if (startTime == null || endTime == null) {
            return 0;
        }
        long diffMillis = endTime.getTime() - startTime.getTime();
        return diffMillis / (1000.0 * 60 * 60);
    }
}