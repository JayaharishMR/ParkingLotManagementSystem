package org.example.parking.models;

import org.example.parking.enums.SpotStatus;
import org.example.parking.enums.SpotType;

import java.util.*;
import java.util.stream.Collectors;

public class ParkingFloor {
    private int floorNumber;
    private Map<SpotType, List<ParkingSpot>> spotsByType;
    private int totalSpots;
    private int occupiedSpots;

    public ParkingFloor(int floorNumber) {
        this.floorNumber = floorNumber;
        this.spotsByType = new HashMap<>();
        this.totalSpots = 0;
        this.occupiedSpots = 0;

        for (SpotType type : SpotType.values()) {
            spotsByType.put(type, new ArrayList<>());
        }
    }

    public synchronized void addSpot(ParkingSpot spot) {
        spotsByType.get(spot.getSpotType()).add(spot);
        totalSpots++;
    }

    public synchronized List<ParkingSpot> getAvailableSpotsByType(SpotType spotType) {
        return spotsByType.get(spotType).stream()
                .filter(spot -> spot.getSpotStatus() == SpotStatus.AVAILABLE)
                .sorted(Comparator.comparingInt(ParkingSpot::getDistanceFromEntrance))
                .collect(Collectors.toList());
    }

    public synchronized ParkingSpot findNearestAvailableSpot(SpotType spotType) {
        List<ParkingSpot> availableSpots = getAvailableSpotsByType(spotType);
        return availableSpots.isEmpty() ? null : availableSpots.get(0);
    }

    public synchronized int getAvailableSpotCount(SpotType spotType) {
        return (int) spotsByType.get(spotType).stream()
                .filter(spot -> spot.getSpotStatus() == SpotStatus.AVAILABLE)
                .count();
    }

    public synchronized double getOccupancyPercentage() {
        if (totalSpots == 0) return 0.0;
        updateOccupiedCount();
        return (occupiedSpots * 100.0) / totalSpots;
    }

    private void updateOccupiedCount() {
        occupiedSpots = 0;
        for (List<ParkingSpot> spots : spotsByType.values()) {
            occupiedSpots += spots.stream()
                    .filter(spot -> spot.getSpotStatus() == SpotStatus.OCCUPIED)
                    .count();
        }
    }

    public int getFloorNumber() {
        return floorNumber;
    }

    public int getTotalSpots() {
        return totalSpots;
    }

    public int getOccupiedSpots() {
        updateOccupiedCount();
        return occupiedSpots;
    }

    public Map<SpotType, List<ParkingSpot>> getSpotsByType() {
        return spotsByType;
    }

    @Override
    public String toString() {
        return "ParkingFloor{" +
                "floorNumber=" + floorNumber +
                ", totalSpots=" + totalSpots +
                ", occupiedSpots=" + getOccupiedSpots() +
                ", occupancy=" + String.format("%.1f%%", getOccupancyPercentage()) +
                '}';
    }
}
