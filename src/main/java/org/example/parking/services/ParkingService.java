package org.example.parking.services;

import org.example.parking.enums.VehicleType;
import org.example.parking.exceptions.*;
import org.example.parking.factory.ParkingTicketGenerator;
import org.example.parking.models.*;
import org.example.parking.repository.ParkingTicketRepository;
import org.example.parking.repository.VehicleRepository;
import org.example.parking.strategies.FeeCalculationStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.List;

@Service
public class ParkingService {

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private ParkingTicketRepository ticketRepository;

    @Autowired
    private FeeCalculationStrategy feeCalculationStrategy;

    private ParkingLot parkingLot;

    @PostConstruct
    public void init() {
        // Initialize the parking lot (in-memory)
        parkingLot = ParkingLot.getInstance();
        initializeParkingLot();
    }

    private void initializeParkingLot() {
        // Configure parking lot: 4 floors, 40 compact, 30 medium, 10 large per floor
        final int TOTAL_FLOORS = 4;
        final int COMPACT_SPOTS_PER_FLOOR = 40;
        final int MEDIUM_SPOTS_PER_FLOOR = 30;
        final int LARGE_SPOTS_PER_FLOOR = 10;

        for (int floorNum = 1; floorNum <= TOTAL_FLOORS; floorNum++) {
            ParkingFloor floor = new ParkingFloor(floorNum);

            int distance = 1;

            // Add Compact spots
            for (int i = 1; i <= COMPACT_SPOTS_PER_FLOOR; i++) {
                String spotId = "F" + floorNum + "-C" + i;
                floor.addSpot(new ParkingSpot(spotId, org.example.parking.enums.SpotType.COMPACT, floorNum, distance++));
            }

            // Add Medium spots
            for (int i = 1; i <= MEDIUM_SPOTS_PER_FLOOR; i++) {
                String spotId = "F" + floorNum + "-M" + i;
                floor.addSpot(new ParkingSpot(spotId, org.example.parking.enums.SpotType.MEDIUM, floorNum, distance++));
            }

            // Add Large spots
            for (int i = 1; i <= LARGE_SPOTS_PER_FLOOR; i++) {
                String spotId = "F" + floorNum + "-L" + i;
                floor.addSpot(new ParkingSpot(spotId, org.example.parking.enums.SpotType.LARGE, floorNum, distance++));
            }

            parkingLot.addFloor(floor);
        }
    }

    @Transactional
    public ParkingTicket parkVehicle(String licensePlate, VehicleType vehicleType)
            throws NoAvailableSpotException, VehicleAlreadyParkedException {

        // Check if vehicle is already parked
        if (ticketRepository.existsByVehicleLicensePlateAndExitTimeIsNull(licensePlate)) {
            throw new VehicleAlreadyParkedException("Vehicle " + licensePlate + " is already parked");
        }

        // Create or get vehicle
        Vehicle vehicle = vehicleRepository.findById(licensePlate)
                .orElseGet(() -> {
                    Vehicle newVehicle = new Vehicle(licensePlate, vehicleType);
                    return vehicleRepository.save(newVehicle);
                });

        // Find optimal parking spot (using in-memory parking lot)
        ParkingSpot spot = parkingLot.findOptimalSpot(vehicle);

        // Assign vehicle to spot
        spot.assignVehicle(vehicle);

        // Generate and save parking ticket
        ParkingTicket ticket = ParkingTicketGenerator.generateTicket(vehicle, spot);
        ticket = ticketRepository.save(ticket);

        // Add to parking lot's active tickets
        parkingLot.addTicket(ticket);

        return ticket;
    }

    @Transactional
    public ParkingTicket unparkVehicle(String ticketId)
            throws InvalidTicketException, TicketAlreadyProcessedException {

        // Find ticket
        ParkingTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new InvalidTicketException("Ticket not found: " + ticketId));

        if (ticket.getExitTime() != null) {
            throw new TicketAlreadyProcessedException("Ticket already processed: " + ticketId);
        }

        // Mark exit
        ticket.markExit();

        // Calculate fee
        double fee = feeCalculationStrategy.calculateFee(ticket);
        ticket.setFee(fee);
        ticket.markAsPaid();

        // Save updated ticket
        ticket = ticketRepository.save(ticket);

        // Remove vehicle from spot (in-memory)
        ParkingTicket lotTicket = parkingLot.getTicket(ticketId);
        if (lotTicket != null && lotTicket.getAssignedSpot() != null) {
            lotTicket.getAssignedSpot().removeVehicle();
            parkingLot.removeTicket(ticketId);
        }

        return ticket;
    }

    public ParkingTicket getTicket(String ticketId) throws InvalidTicketException {
        return ticketRepository.findById(ticketId)
                .orElseThrow(() -> new InvalidTicketException("Ticket not found: " + ticketId));
    }

    public List<ParkingTicket> getAllActiveTickets() {
        return ticketRepository.findAllActiveTickets();
    }

    public List<ParkingTicket> getAllTickets() {
        return ticketRepository.findAll();
    }

    public ParkingLot getParkingLot() {
        return parkingLot;
    }

    public double getOverallOccupancy() {
        return parkingLot.getOverallOccupancy();
    }
}
