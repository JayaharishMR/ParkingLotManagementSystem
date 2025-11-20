package org.example.parking.models;

import org.example.parking.enums.SpotStatus;
import org.example.parking.enums.SpotType;
import org.example.parking.enums.VehicleType;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class ParkingSpot {
    private String spotId;
    private SpotType spotType;
    private SpotStatus spotStatus;
    private int floorNumber;
    private int distanceFromEntrance;
    private Vehicle currentVehicle;
    private LocalDateTime lastOccupiedTime;

    // Configuration: Vehicle type to Spot type mapping
    private static final Map<VehicleType, SpotType> VEHICLE_SPOT_MAPPING = new HashMap<>();

    static {
        VEHICLE_SPOT_MAPPING.put(VehicleType.MOTORCYCLE, SpotType.COMPACT);
        VEHICLE_SPOT_MAPPING.put(VehicleType.CAR, SpotType.MEDIUM);
        VEHICLE_SPOT_MAPPING.put(VehicleType.BUS, SpotType.LARGE);
    }

    public ParkingSpot(String spotId, SpotType spotType, int floorNumber, int distanceFromEntrance) {
        this.spotId = spotId;
        this.spotType = spotType;
        this.floorNumber = floorNumber;
        this.distanceFromEntrance = distanceFromEntrance;
        this.spotStatus = SpotStatus.AVAILABLE;
        this.currentVehicle = null;
        this.lastOccupiedTime = null;
    }

    public boolean canParkVehicle(Vehicle vehicle) {
        if (spotStatus != SpotStatus.AVAILABLE) {
            return false;
        }

        SpotType requiredSpotType = VEHICLE_SPOT_MAPPING.get(vehicle.getVehicleType());
        return this.spotType == requiredSpotType;
    }

    public synchronized boolean assignVehicle(Vehicle vehicle) {
        if (canParkVehicle(vehicle)) {
            this.currentVehicle = vehicle;
            this.spotStatus = SpotStatus.OCCUPIED;
            this.lastOccupiedTime = LocalDateTime.now();
            return true;
        }
        return false;
    }

    public synchronized void removeVehicle() {
        this.currentVehicle = null;
        this.spotStatus = SpotStatus.AVAILABLE;
    }

    public String getSpotId() {
        return spotId;
    }

    public SpotType getSpotType() {
        return spotType;
    }

    public SpotStatus getSpotStatus() {
        return spotStatus;
    }

    public int getFloorNumber() {
        return floorNumber;
    }

    public int getDistanceFromEntrance() {
        return distanceFromEntrance;
    }

    public Vehicle getCurrentVehicle() {
        return currentVehicle;
    }

    public LocalDateTime getLastOccupiedTime() {
        return lastOccupiedTime;
    }

    @Override
    public String toString() {
        return "ParkingSpot{" +
                "spotId='" + spotId + '\'' +
                ", spotType=" + spotType +
                ", spotStatus=" + spotStatus +
                ", floorNumber=" + floorNumber +
                ", currentVehicle=" + currentVehicle +
                '}';
    }
}
