package com.example.parkingfinder.database.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "parking_spots",
        foreignKeys = {
                @ForeignKey(
                        entity = ParkingAreaEntity.class,
                        parentColumns = "id",
                        childColumns = "parkingAreaId",
                        onDelete = ForeignKey.CASCADE
                )
        },
        indices = {@Index("parkingAreaId")}
)
public class ParkingSpotEntity {
    @PrimaryKey
    @NonNull
    private String id;
    @NonNull
    private String parkingAreaId;
    private String spotNumber;
    private int floor;
    private String section;
    private boolean available;
    private boolean isReserved;
    private boolean isHandicapped;
    private boolean isElectricCharging;
    private int positionX;
    private int positionY;
    private String type;
    private long lastUpdated;

    public ParkingSpotEntity(@NonNull String id, @NonNull String parkingAreaId, String spotNumber,
                             int floor, String section, boolean available, boolean isReserved,
                             boolean isHandicapped, boolean isElectricCharging,
                             int positionX, int positionY, String type) {
        this.id = id;
        this.parkingAreaId = parkingAreaId;
        this.spotNumber = spotNumber;
        this.floor = floor;
        this.section = section;
        this.available = available;
        this.isReserved = isReserved;
        this.isHandicapped = isHandicapped;
        this.isElectricCharging = isElectricCharging;
        this.positionX = positionX;
        this.positionY = positionY;
        this.type = type;
        this.lastUpdated = System.currentTimeMillis();
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    @NonNull
    public String getParkingAreaId() {
        return parkingAreaId;
    }

    public void setParkingAreaId(@NonNull String parkingAreaId) {
        this.parkingAreaId = parkingAreaId;
    }

    public String getSpotNumber() {
        return spotNumber;
    }

    public void setSpotNumber(String spotNumber) {
        this.spotNumber = spotNumber;
    }

    public int getFloor() {
        return floor;
    }

    public void setFloor(int floor) {
        this.floor = floor;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public boolean isReserved() {
        return isReserved;
    }

    public void setReserved(boolean reserved) {
        isReserved = reserved;
    }

    public boolean isHandicapped() {
        return isHandicapped;
    }

    public void setHandicapped(boolean handicapped) {
        isHandicapped = handicapped;
    }

    public boolean isElectricCharging() {
        return isElectricCharging;
    }

    public void setElectricCharging(boolean electricCharging) {
        isElectricCharging = electricCharging;
    }

    public int getPositionX() {
        return positionX;
    }

    public void setPositionX(int positionX) {
        this.positionX = positionX;
    }

    public int getPositionY() {
        return positionY;
    }

    public void setPositionY(int positionY) {
        this.positionY = positionY;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}