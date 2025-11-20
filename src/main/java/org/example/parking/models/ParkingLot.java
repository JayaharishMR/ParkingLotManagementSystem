package org.example.parking.models;

import org.example.parking.enums.SpotType;
import org.example.parking.enums.VehicleType;
import org.example.parking.exceptions.NoAvailableSpotException;
import org.example.parking.exceptions.VehicleAlreadyParkedException;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ParkingLot {
    private static ParkingLot instance;
    private List<ParkingFloor> floors;
    private Map<String, ParkingTicket> activeTickets;
    private Map<String, ParkingTicket> vehicleTicketMap; // Track vehicles currently parked

    // Vehicle to Spot type mapping (configuration)
    private static final Map<VehicleType, SpotType> VEHICLE_SPOT_MAPPING = new HashMap<>();

    static {
        VEHICLE_SPOT_MAPPING.put(VehicleType.MOTORCYCLE, SpotType.COMPACT);
        VEHICLE_SPOT_MAPPING.put(VehicleType.CAR, SpotType.MEDIUM);
        VEHICLE_SPOT_MAPPING.put(VehicleType.BUS, SpotType.LARGE);
    }

    private ParkingLot() {
        this.floors = new ArrayList<>();
        this.activeTickets = new ConcurrentHashMap<>();
        this.vehicleTicketMap = new ConcurrentHashMap<>();
    }

    public static synchronized ParkingLot getInstance() {
        if (instance == null) {
            instance = new ParkingLot();
        }
        return instance;
    }

    public synchronized void addFloor(ParkingFloor floor) {
        floors.add(floor);
    }

    public synchronized boolean isVehicleAlreadyParked(Vehicle vehicle) {
        return vehicleTicketMap.containsKey(vehicle.getLicensePlate());
    }

    public synchronized ParkingSpot findOptimalSpot(Vehicle vehicle) throws NoAvailableSpotException, VehicleAlreadyParkedException {
        // Check if vehicle is already parked
        if (isVehicleAlreadyParked(vehicle)) {
            throw new VehicleAlreadyParkedException("Vehicle " + vehicle.getLicensePlate() + " is already parked");
        }

        SpotType requiredSpotType = VEHICLE_SPOT_MAPPING.get(vehicle.getVehicleType());

        // Find floor with lowest occupancy that has available spots
        ParkingFloor selectedFloor = selectFloorWithLowestOccupancy(requiredSpotType);

        if (selectedFloor == null) {
            throw new NoAvailableSpotException("No available spots for vehicle type: " + vehicle.getVehicleType());
        }

        // Find nearest available spot on selected floor
        ParkingSpot spot = selectedFloor.findNearestAvailableSpot(requiredSpotType);

        if (spot == null) {
            throw new NoAvailableSpotException("No available spots for vehicle type: " + vehicle.getVehicleType());
        }

        return spot;
    }

    private ParkingFloor selectFloorWithLowestOccupancy(SpotType spotType) {
        return floors.stream()
                .filter(floor -> floor.getAvailableSpotCount(spotType) > 0)
                .min(Comparator.comparingDouble(ParkingFloor::getOccupancyPercentage))
                .orElse(null);
    }

    public synchronized void addTicket(ParkingTicket ticket) {
        activeTickets.put(ticket.getTicketId(), ticket);
        vehicleTicketMap.put(ticket.getVehicle().getLicensePlate(), ticket);
    }

    public synchronized ParkingTicket getTicket(String ticketId) {
        return activeTickets.get(ticketId);
    }

    public synchronized void removeTicket(String ticketId) {
        ParkingTicket ticket = activeTickets.remove(ticketId);
        if (ticket != null) {
            vehicleTicketMap.remove(ticket.getVehicle().getLicensePlate());
        }
    }

    public synchronized double getOverallOccupancy() {
        if (floors.isEmpty()) return 0.0;

        int totalSpots = 0;
        int totalOccupied = 0;

        for (ParkingFloor floor : floors) {
            totalSpots += floor.getTotalSpots();
            totalOccupied += floor.getOccupiedSpots();
        }

        return totalSpots > 0 ? (totalOccupied * 100.0) / totalSpots : 0.0;
    }

    public List<ParkingFloor> getFloors() {
        return new ArrayList<>(floors);
    }

    public Map<String, ParkingTicket> getActiveTickets() {
        return new HashMap<>(activeTickets);
    }

    public int getTotalActiveTickets() {
        return activeTickets.size();
    }

    @Override
    public String toString() {
        return "ParkingLot{" +
                "floors=" + floors.size() +
                ", activeTickets=" + activeTickets.size() +
                ", overallOccupancy=" + String.format("%.1f%%", getOverallOccupancy()) +
                '}';
    }
}
