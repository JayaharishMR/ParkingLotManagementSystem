package org.example.parking.exceptions;

public class TicketAlreadyProcessedException extends Exception {
    public TicketAlreadyProcessedException(String message) {
        super(message);
    }
}
