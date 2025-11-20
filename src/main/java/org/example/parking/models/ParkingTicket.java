package org.example.parking.models;

import javax.persistence.*;
import java.time.Duration;
import java.time.LocalDateTime;

@Entity
@Table(name = "parking_tickets")
public class ParkingTicket {

    @Id
    @Column(name = "ticket_id", nullable = false, length = 50)
    private String ticketId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "vehicle_license", nullable = false)
    private Vehicle vehicle;

    @Column(name = "spot_id", nullable = false, length = 50)
    private String spotId;

    @Column(name = "floor_number", nullable = false)
    private int floorNumber;

    @Column(name = "entry_time", nullable = false)
    private LocalDateTime entryTime;

    @Column(name = "exit_time")
    private LocalDateTime exitTime;

    @Column(name = "fee")
    private double fee;

    @Column(name = "is_paid")
    private boolean isPaid;

    // Transient field - not persisted
    @Transient
    private ParkingSpot assignedSpot;

    public ParkingTicket() {
    }

    public ParkingTicket(String ticketId, Vehicle vehicle, ParkingSpot assignedSpot) {
        this.ticketId = ticketId;
        this.vehicle = vehicle;
        this.assignedSpot = assignedSpot;
        this.spotId = assignedSpot.getSpotId();
        this.floorNumber = assignedSpot.getFloorNumber();
        this.entryTime = LocalDateTime.now();
        this.exitTime = null;
        this.fee = 0.0;
        this.isPaid = false;
    }

    public long getParkingDurationInHours() {
        LocalDateTime endTime = (exitTime != null) ? exitTime : LocalDateTime.now();
        Duration duration = Duration.between(entryTime, endTime);
        long hours = duration.toHours();
        if (duration.toMinutesPart() > 0) {
            hours++;
        }
        return hours > 0 ? hours : 1;
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

    // Getters and setters
    public String getTicketId() {
        return ticketId;
    }

    public void setTicketId(String ticketId) {
        this.ticketId = ticketId;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public ParkingSpot getAssignedSpot() {
        return assignedSpot;
    }

    public void setAssignedSpot(ParkingSpot assignedSpot) {
        this.assignedSpot = assignedSpot;
        if (assignedSpot != null) {
            this.spotId = assignedSpot.getSpotId();
            this.floorNumber = assignedSpot.getFloorNumber();
        }
    }

    public String getSpotId() {
        return spotId;
    }

    public void setSpotId(String spotId) {
        this.spotId = spotId;
    }

    public int getFloorNumber() {
        return floorNumber;
    }

    public void setFloorNumber(int floorNumber) {
        this.floorNumber = floorNumber;
    }

    public LocalDateTime getEntryTime() {
        return entryTime;
    }

    public void setEntryTime(LocalDateTime entryTime) {
        this.entryTime = entryTime;
    }

    public LocalDateTime getExitTime() {
        return exitTime;
    }

    public void setExitTime(LocalDateTime exitTime) {
        this.exitTime = exitTime;
    }

    public double getFee() {
        return fee;
    }

    public boolean isPaid() {
        return isPaid;
    }

    public void setPaid(boolean paid) {
        isPaid = paid;
    }

    @Override
    public String toString() {
        return "ParkingTicket{" +
                "ticketId='" + ticketId + '\'' +
                ", vehicle=" + vehicle +
                ", spotId=" + spotId +
                ", floor=" + floorNumber +
                ", entryTime=" + entryTime +
                ", exitTime=" + exitTime +
                ", fee=" + fee +
                ", isPaid=" + isPaid +
                '}';
    }
}
