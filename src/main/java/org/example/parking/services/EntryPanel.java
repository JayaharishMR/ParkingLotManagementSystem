package org.example.parking.services;

import org.example.parking.dao.DatabaseManager;
import org.example.parking.exceptions.DatabaseException;
import org.example.parking.exceptions.NoAvailableSpotException;
import org.example.parking.exceptions.VehicleAlreadyParkedException;
import org.example.parking.factory.ParkingTicketGenerator;
import org.example.parking.models.ParkingLot;
import org.example.parking.models.ParkingSpot;
import org.example.parking.models.ParkingTicket;
import org.example.parking.models.Vehicle;

public class EntryPanel {
    private String panelId;
    private ParkingLot parkingLot;
    private DatabaseManager dbManager;

    public EntryPanel(String panelId, ParkingLot parkingLot, DatabaseManager dbManager) {
        this.panelId = panelId;
        this.parkingLot = parkingLot;
        this.dbManager = dbManager;
    }

    public synchronized ParkingTicket processEntry(Vehicle vehicle) throws NoAvailableSpotException, VehicleAlreadyParkedException {
        System.out.println("\n[" + panelId + "] Processing entry for vehicle: " + vehicle.getLicensePlate());

        // Find optimal parking spot
        ParkingSpot spot = parkingLot.findOptimalSpot(vehicle);

        // Assign vehicle to spot
        boolean assigned = spot.assignVehicle(vehicle);

        if (!assigned) {
            throw new NoAvailableSpotException("Failed to assign spot to vehicle");
        }

        // Generate parking ticket
        ParkingTicket ticket = ParkingTicketGenerator.generateTicket(vehicle, spot);

        // Add ticket to parking lot
        parkingLot.addTicket(ticket);

        // Persist to database
        try {
            dbManager.saveVehicle(vehicle.getLicensePlate(), vehicle.getVehicleType().toString());
            dbManager.saveParkingTicket(
                ticket.getTicketId(),
                vehicle.getLicensePlate(),
                spot.getSpotId(),
                spot.getFloorNumber(),
                ticket.getEntryTime(),
                0.0,
                false
            );
            dbManager.updateParkingSpot(
                spot.getSpotId(),
                spot.getSpotStatus().toString(),
                vehicle.getLicensePlate(),
                spot.getLastOccupiedTime()
            );
        } catch (DatabaseException e) {
            System.err.println("Warning: Failed to persist to database: " + e.getMessage());
        }

        System.out.println("[" + panelId + "] Vehicle parked successfully!");
        System.out.println("Ticket ID: " + ticket.getTicketId());
        System.out.println("Spot: " + spot.getSpotId() + " (Floor " + spot.getFloorNumber() + ")");
        System.out.println("Entry Time: " + ticket.getEntryTime());

        return ticket;
    }

    public String getPanelId() {
        return panelId;
    }
}
