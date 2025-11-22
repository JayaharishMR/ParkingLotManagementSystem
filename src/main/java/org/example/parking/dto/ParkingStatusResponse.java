package org.example.parking.dto;

public class ParkingStatusResponse {
    private double overallOccupancy;
    private int activeTickets;
    private int totalFloors;

    public ParkingStatusResponse() {
    }

    public double getOverallOccupancy() {
        return overallOccupancy;
    }

    public void setOverallOccupancy(double overallOccupancy) {
        this.overallOccupancy = overallOccupancy;
    }

    public int getActiveTickets() {
        return activeTickets;
    }

    public void setActiveTickets(int activeTickets) {
        this.activeTickets = activeTickets;
    }

    public int getTotalFloors() {
        return totalFloors;
    }

    public void setTotalFloors(int totalFloors) {
        this.totalFloors = totalFloors;
    }
}
