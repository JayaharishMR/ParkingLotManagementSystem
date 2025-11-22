package org.example.parking.factory;

import org.example.parking.models.ParkingSpot;
import org.example.parking.models.ParkingTicket;
import org.example.parking.models.Vehicle;

import java.util.UUID;

public class ParkingTicketGenerator {

    public static ParkingTicket generateTicket(Vehicle vehicle, ParkingSpot spot) {
        String ticketId = generateUniqueTicketId();
        return new ParkingTicket(ticketId, vehicle, spot);
    }

    private static String generateUniqueTicketId() {
        return "TKT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
