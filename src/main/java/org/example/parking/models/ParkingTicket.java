package org.example.parking.models;

import java.time.LocalDateTime;
import java.time.Duration;

public class ParkingTicket {
    private String ticketId;
    private Vehicle vehicle;
    private ParkingSpot assignedSpot;
    private LocalDateTime entryTime;
    private LocalDateTime exitTime;
    private double fee;
    private boolean isPaid;

    public ParkingTicket(String ticketId, Vehicle vehicle, ParkingSpot assignedSpot) {
        this.ticketId = ticketId;
        this.vehicle = vehicle;
        this.assignedSpot = assignedSpot;
        this.entryTime = LocalDateTime.now();
        this.exitTime = null;
        this.fee = 0.0;
        this.isPaid = false;
    }

    public long getParkingDurationInHours() {
        LocalDateTime endTime = (exitTime != null) ? exitTime : LocalDateTime.now();
        Duration duration = Duration.between(entryTime, endTime);
        long hours = duration.toHours();
        // Round up if there are remaining minutes
        if (duration.toMinutesPart() > 0) {
            hours++;
        }
        return hours > 0 ? hours : 1; // Minimum 1 hour
    }

    public void markExit() {
        this.exitTime = LocalDateTime.now();
    }

    public void setFee(double fee) {
        this.fee = fee;
    }

    public void markAsPaid() {
        this.isPaid = true;
    }

    public String getTicketId() {
        return ticketId;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public ParkingSpot getAssignedSpot() {
        return assignedSpot;
    }

    public String getSpotId() {
        return assignedSpot.getSpotId();
    }

    public int getFloorNumber() {
        return assignedSpot.getFloorNumber();
    }

    public LocalDateTime getEntryTime() {
        return entryTime;
    }

    public LocalDateTime getExitTime() {
        return exitTime;
    }

    public double getFee() {
        return fee;
    }

    public boolean isPaid() {
        return isPaid;
    }

    @Override
    public String toString() {
        return "ParkingTicket{" +
                "ticketId='" + ticketId + '\'' +
                ", vehicle=" + vehicle +
                ", spot=" + assignedSpot.getSpotId() +
                ", floor=" + assignedSpot.getFloorNumber() +
                ", entryTime=" + entryTime +
                ", exitTime=" + exitTime +
                ", fee=" + fee +
                ", isPaid=" + isPaid +
                '}';
    }
}
