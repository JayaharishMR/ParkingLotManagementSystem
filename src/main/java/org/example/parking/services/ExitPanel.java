package org.example.parking.services;

import org.example.parking.dao.DatabaseManager;
import org.example.parking.enums.PaymentMethod;
import org.example.parking.exceptions.DatabaseException;
import org.example.parking.exceptions.InvalidTicketException;
import org.example.parking.exceptions.TicketAlreadyProcessedException;
import org.example.parking.models.ParkingLot;
import org.example.parking.models.ParkingSpot;
import org.example.parking.models.ParkingTicket;

public class ExitPanel {
    private String panelId;
    private ParkingLot parkingLot;
    private PaymentProcessor paymentProcessor;
    private DatabaseManager dbManager;

    public ExitPanel(String panelId, ParkingLot parkingLot, PaymentProcessor paymentProcessor, DatabaseManager dbManager) {
        this.panelId = panelId;
        this.parkingLot = parkingLot;
        this.paymentProcessor = paymentProcessor;
        this.dbManager = dbManager;
    }

    public synchronized double processExit(String ticketId) throws InvalidTicketException, TicketAlreadyProcessedException {
        System.out.println("\n[" + panelId + "] Processing exit for ticket: " + ticketId);

        // Retrieve ticket
        ParkingTicket ticket = parkingLot.getTicket(ticketId);

        if (ticket == null) {
            throw new InvalidTicketException("Ticket not found: " + ticketId);
        }

        if (ticket.getExitTime() != null) {
            throw new TicketAlreadyProcessedException("Ticket already processed: " + ticketId);
        }

        // Mark exit time
        ticket.markExit();

        // Calculate fee
        double fee = paymentProcessor.calculateFee(ticket);

        System.out.println("Vehicle: " + ticket.getVehicle().getLicensePlate());
        System.out.println("Parking Duration: " + ticket.getParkingDurationInHours() + " hour(s)");
        System.out.println("Total Fee: Rs. " + fee);

        // Process payment (currently cash only)
        boolean paymentSuccess = paymentProcessor.processPayment(ticket, PaymentMethod.CASH);

        if (!paymentSuccess) {
            throw new RuntimeException("Payment processing failed");
        }

        // Remove vehicle from spot
        ParkingSpot spot = ticket.getAssignedSpot();
        spot.removeVehicle();

        // Remove ticket from active tickets
        parkingLot.removeTicket(ticketId);

        // Persist to database
        try {
            dbManager.updateParkingTicket(
                ticketId,
                ticket.getExitTime(),
                fee,
                true
            );
            dbManager.updateParkingSpot(
                spot.getSpotId(),
                spot.getSpotStatus().toString(),
                null,
                null
            );
        } catch (DatabaseException e) {
            System.err.println("Warning: Failed to persist to database: " + e.getMessage());
        }

        System.out.println("[" + panelId + "] Vehicle exited successfully!");

        return fee;
    }

    public String getPanelId() {
        return panelId;
    }
}
