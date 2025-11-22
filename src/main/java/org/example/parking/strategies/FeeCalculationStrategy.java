package org.example.parking.strategies;

import org.example.parking.models.ParkingTicket;

public interface FeeCalculationStrategy {
    double calculateFee(ParkingTicket ticket);
}
