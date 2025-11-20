package org.example.parking;

import org.example.parking.dao.DatabaseManager;
import org.example.parking.enums.SpotType;
import org.example.parking.enums.VehicleType;
import org.example.parking.exceptions.*;
import org.example.parking.models.*;
import org.example.parking.services.EntryPanel;
import org.example.parking.services.ExitPanel;
import org.example.parking.services.PaymentProcessor;
import org.example.parking.strategies.CashPaymentStrategy;
import org.example.parking.strategies.TieredPricingStrategy;

import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    private static ParkingLot parkingLot;
    private static EntryPanel entryPanel;
    private static ExitPanel exitPanel;
    private static DatabaseManager dbManager;
    private static Scanner scanner;

    // Hardcoded configuration
    private static final int TOTAL_FLOORS = 4;
    private static final int COMPACT_SPOTS_PER_FLOOR = 40;
    private static final int MEDIUM_SPOTS_PER_FLOOR = 30;
    private static final int LARGE_SPOTS_PER_FLOOR = 10;

    public static void main(String[] args) {
        try {
            // Initialize system
            initializeSystem();

            // Start interactive CLI
            runInteractiveCLI();

        } catch (Exception e) {
            System.err.println("Fatal error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            cleanup();
        }
    }

    private static void initializeSystem() throws DatabaseException {
        System.out.println("========================================");
        System.out.println("  PARKING LOT MANAGEMENT SYSTEM");
        System.out.println("========================================\n");

        // Initialize Database
        dbManager = new DatabaseManager();
        dbManager.initializeSchema();

        // Start H2 Console
        dbManager.startH2Console();

        // Initialize Parking Lot
        parkingLot = initializeParkingLot();
        System.out.println("\nParking Lot initialized with " + TOTAL_FLOORS + " floors");
        System.out.println("Spots per floor: " + COMPACT_SPOTS_PER_FLOOR + " Compact, " +
                MEDIUM_SPOTS_PER_FLOOR + " Medium, " + LARGE_SPOTS_PER_FLOOR + " Large");

        // Populate parking spots in database
        populateParkingSpotsInDB();

        // Create Payment Processor
        PaymentProcessor paymentProcessor = new PaymentProcessor(
                new TieredPricingStrategy(),
                new CashPaymentStrategy()
        );

        // Create Entry and Exit Panels
        entryPanel = new EntryPanel("ENTRY-1", parkingLot, dbManager);
        exitPanel = new ExitPanel("EXIT-1", parkingLot, paymentProcessor, dbManager);

        scanner = new Scanner(System.in);

        System.out.println("\nSystem ready!\n");
    }

    private static ParkingLot initializeParkingLot() {
        ParkingLot lot = ParkingLot.getInstance();

        for (int floorNum = 1; floorNum <= TOTAL_FLOORS; floorNum++) {
            ParkingFloor floor = new ParkingFloor(floorNum);

            int distance = 1;

            // Add Compact spots
            for (int i = 1; i <= COMPACT_SPOTS_PER_FLOOR; i++) {
                String spotId = "F" + floorNum + "-C" + i;
                floor.addSpot(new ParkingSpot(spotId, SpotType.COMPACT, floorNum, distance++));
            }

            // Add Medium spots
            for (int i = 1; i <= MEDIUM_SPOTS_PER_FLOOR; i++) {
                String spotId = "F" + floorNum + "-M" + i;
                floor.addSpot(new ParkingSpot(spotId, SpotType.MEDIUM, floorNum, distance++));
            }

            // Add Large spots
            for (int i = 1; i <= LARGE_SPOTS_PER_FLOOR; i++) {
                String spotId = "F" + floorNum + "-L" + i;
                floor.addSpot(new ParkingSpot(spotId, SpotType.LARGE, floorNum, distance++));
            }

            lot.addFloor(floor);
        }

        return lot;
    }

    private static void populateParkingSpotsInDB() {
        System.out.println("Populating parking data in database...");

        try {
            // First, populate parking floors
            for (ParkingFloor floor : parkingLot.getFloors()) {
                String floorSql = "INSERT INTO parking_floors (floor_number, total_spots, occupied_spots) VALUES (?, ?, ?)";
                try (java.sql.Connection conn = dbManager.getConnection();
                     java.sql.PreparedStatement stmt = conn.prepareStatement(floorSql)) {
                    stmt.setInt(1, floor.getFloorNumber());
                    stmt.setInt(2, floor.getTotalSpots());
                    stmt.setInt(3, 0);
                    stmt.executeUpdate();
                }
            }

            // Then, populate parking spots
            for (ParkingFloor floor : parkingLot.getFloors()) {
                for (SpotType spotType : SpotType.values()) {
                    for (ParkingSpot spot : floor.getSpotsByType().get(spotType)) {
                        String insertSql = "INSERT INTO parking_spots (spot_id, spot_type, spot_status, " +
                                "floor_number, distance_from_entrance, vehicle_license, last_occupied_time) " +
                                "VALUES (?, ?, ?, ?, ?, ?, ?)";

                        try (java.sql.Connection conn = dbManager.getConnection();
                             java.sql.PreparedStatement stmt = conn.prepareStatement(insertSql)) {

                            stmt.setString(1, spot.getSpotId());
                            stmt.setString(2, spot.getSpotType().toString());
                            stmt.setString(3, spot.getSpotStatus().toString());
                            stmt.setInt(4, spot.getFloorNumber());
                            stmt.setInt(5, spot.getDistanceFromEntrance());
                            stmt.setString(6, null);
                            stmt.setTimestamp(7, null);
                            stmt.executeUpdate();
                        }
                    }
                }
            }
            System.out.println("Parking data populated successfully!");
            System.out.println("  - " + parkingLot.getFloors().size() + " floors");
            System.out.println("  - " + (TOTAL_FLOORS * (COMPACT_SPOTS_PER_FLOOR + MEDIUM_SPOTS_PER_FLOOR + LARGE_SPOTS_PER_FLOOR)) + " parking spots");
        } catch (Exception e) {
            System.err.println("Failed to populate parking data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void runInteractiveCLI() {
        while (true) {
            displayMainMenu();
            int choice = getValidIntInput();

            try {
                switch (choice) {
                    case 1:
                        parkVehicle();
                        break;
                    case 2:
                        unparkVehicle();
                        break;
                    case 3:
                        displayStatus();
                        break;
                    case 4:
                        searchTicket();
                        break;
                    case 5:
                        testScenariosMenu();
                        break;
                    case 6:
                        System.out.println("\nThank you for using the Parking Lot Management System!");
                        return;
                    default:
                        System.out.println("Invalid choice! Please try again.");
                }
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }

            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
        }
    }

    private static void displayMainMenu() {
        System.out.println("\n╔═══════════════════════════════════════╗");
        System.out.println("║   PARKING LOT MANAGEMENT SYSTEM      ║");
        System.out.println("╠═══════════════════════════════════════╣");
        System.out.println("║  1. Park Vehicle                     ║");
        System.out.println("║  2. Unpark Vehicle                   ║");
        System.out.println("║  3. Display Status                   ║");
        System.out.println("║  4. Search Ticket                    ║");
        System.out.println("║  5. Test Scenarios                   ║");
        System.out.println("║  6. Exit                             ║");
        System.out.println("╚═══════════════════════════════════════╝");
        System.out.print("Enter choice: ");
    }

    private static void parkVehicle() {
        System.out.println("\n--- Park Vehicle ---");
        System.out.print("Enter License Plate: ");
        String licensePlate = scanner.nextLine().trim().toUpperCase();

        if (licensePlate.isEmpty()) {
            System.out.println("License plate cannot be empty!");
            return;
        }

        System.out.println("Select Vehicle Type:");
        System.out.println("1. Motorcycle");
        System.out.println("2. Car");
        System.out.println("3. Bus");
        System.out.print("Enter choice: ");

        int typeChoice = getValidIntInput();
        VehicleType vehicleType;

        switch (typeChoice) {
            case 1:
                vehicleType = VehicleType.MOTORCYCLE;
                break;
            case 2:
                vehicleType = VehicleType.CAR;
                break;
            case 3:
                vehicleType = VehicleType.BUS;
                break;
            default:
                System.out.println("Invalid vehicle type!");
                return;
        }

        Vehicle vehicle = new Vehicle(licensePlate, vehicleType);

        try {
            ParkingTicket ticket = entryPanel.processEntry(vehicle);
            System.out.println("\n✓ SUCCESS! Vehicle parked.");
            System.out.println("Keep this ticket safe: " + ticket.getTicketId());
        } catch (NoAvailableSpotException e) {
            System.out.println("\n✗ ERROR: " + e.getMessage());
        } catch (VehicleAlreadyParkedException e) {
            System.out.println("\n✗ ERROR: " + e.getMessage());
        }
    }

    private static void unparkVehicle() {
        System.out.println("\n--- Unpark Vehicle ---");
        System.out.print("Enter Ticket ID: ");
        String ticketId = scanner.nextLine().trim().toUpperCase();

        if (ticketId.isEmpty()) {
            System.out.println("Ticket ID cannot be empty!");
            return;
        }

        try {
            double fee = exitPanel.processExit(ticketId);
            System.out.println("\n✓ SUCCESS! Vehicle exited. Total fee: Rs. " + fee);
        } catch (InvalidTicketException e) {
            System.out.println("\n✗ ERROR: " + e.getMessage());
        } catch (TicketAlreadyProcessedException e) {
            System.out.println("\n✗ ERROR: " + e.getMessage());
        }
    }

    private static void displayStatus() {
        System.out.println("\n========== PARKING LOT STATUS ==========");
        System.out.println("Overall Occupancy: " + String.format("%.1f%%", parkingLot.getOverallOccupancy()));
        System.out.println("Active Tickets: " + parkingLot.getTotalActiveTickets());
        System.out.println("\nFloor-wise Status:");
        System.out.println("----------------------------------------");

        for (ParkingFloor floor : parkingLot.getFloors()) {
            System.out.printf("\nFloor %d: %.1f%% occupied (%d/%d spots)\n",
                    floor.getFloorNumber(),
                    floor.getOccupancyPercentage(),
                    floor.getOccupiedSpots(),
                    floor.getTotalSpots());

            System.out.printf("  • Compact spots available: %d\n",
                    floor.getAvailableSpotCount(SpotType.COMPACT));
            System.out.printf("  • Medium spots available: %d\n",
                    floor.getAvailableSpotCount(SpotType.MEDIUM));
            System.out.printf("  • Large spots available: %d\n",
                    floor.getAvailableSpotCount(SpotType.LARGE));
        }
        System.out.println("========================================");
    }

    private static void searchTicket() {
        System.out.println("\n--- Search Ticket ---");
        System.out.print("Enter Ticket ID: ");
        String ticketId = scanner.nextLine().trim().toUpperCase();

        ParkingTicket ticket = parkingLot.getTicket(ticketId);

        if (ticket == null) {
            System.out.println("✗ Ticket not found!");
        } else {
            System.out.println("\n✓ Ticket Found:");
            System.out.println("Ticket ID: " + ticket.getTicketId());
            System.out.println("Vehicle: " + ticket.getVehicle().getLicensePlate() +
                    " (" + ticket.getVehicle().getVehicleType() + ")");
            System.out.println("Spot: " + ticket.getSpotId() + " (Floor " + ticket.getFloorNumber() + ")");
            System.out.println("Entry Time: " + ticket.getEntryTime());
            System.out.println("Current Duration: " + ticket.getParkingDurationInHours() + " hour(s)");
        }
    }

    private static void testScenariosMenu() {
        System.out.println("\n╔═══════════════════════════════════════╗");
        System.out.println("║         TEST SCENARIOS MENU          ║");
        System.out.println("╠═══════════════════════════════════════╣");
        System.out.println("║  1. Fill Parking Lot (Test Full)    ║");
        System.out.println("║  2. Test Invalid Ticket              ║");
        System.out.println("║  3. Test Concurrent Entry            ║");
        System.out.println("║  4. Test Double Parking              ║");
        System.out.println("║  5. Back to Main Menu                ║");
        System.out.println("╚═══════════════════════════════════════╝");
        System.out.print("Enter choice: ");

        int choice = getValidIntInput();

        switch (choice) {
            case 1:
                testFillParkingLot();
                break;
            case 2:
                testInvalidTicket();
                break;
            case 3:
                testConcurrentEntry();
                break;
            case 4:
                testDoublePark();
                break;
            case 5:
                return;
            default:
                System.out.println("Invalid choice!");
        }
    }

    private static void testFillParkingLot() {
        System.out.println("\n--- Test: Filling Parking Lot ---");
        System.out.println("This will attempt to park many vehicles until the lot is full.");

        int count = 0;
        for (int i = 0; i < 500; i++) {
            Vehicle v = new Vehicle("TEST-" + i, VehicleType.CAR);
            try {
                entryPanel.processEntry(v);
                count++;
            } catch (NoAvailableSpotException e) {
                System.out.println("\n✓ Parking lot is now FULL for cars!");
                System.out.println("Successfully parked " + count + " test vehicles.");
                break;
            } catch (VehicleAlreadyParkedException e) {
                // Should not happen in this test
            }
        }
    }

    private static void testInvalidTicket() {
        System.out.println("\n--- Test: Invalid Ticket ---");
        String fakeTicketId = "TKT-INVALID";

        try {
            exitPanel.processExit(fakeTicketId);
        } catch (InvalidTicketException e) {
            System.out.println("✓ Test passed! System correctly rejected invalid ticket.");
            System.out.println("Error message: " + e.getMessage());
        } catch (TicketAlreadyProcessedException e) {
            System.out.println("Unexpected error: " + e.getMessage());
        }
    }

    private static void testConcurrentEntry() {
        System.out.println("\n--- Test: Concurrent Vehicle Entry ---");
        System.out.println("Simulating 10 vehicles entering simultaneously...\n");

        ExecutorService executor = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(10);

        for (int i = 0; i < 10; i++) {
            int num = i;
            executor.submit(() -> {
                Vehicle v = new Vehicle("CONCURRENT-" + num, VehicleType.CAR);
                try {
                    ParkingTicket ticket = entryPanel.processEntry(v);
                    System.out.println("✓ Thread " + num + " succeeded: " + ticket.getSpotId());
                } catch (Exception e) {
                    System.out.println("✗ Thread " + num + " failed: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        try {
            latch.await();
            executor.shutdown();
            System.out.println("\n✓ Concurrent test completed!");
        } catch (InterruptedException e) {
            System.out.println("Test interrupted: " + e.getMessage());
        }
    }

    private static void testDoublePark() {
        System.out.println("\n--- Test: Double Parking ---");
        Vehicle v = new Vehicle("DOUBLE-TEST", VehicleType.CAR);

        try {
            // First parking attempt
            ParkingTicket ticket1 = entryPanel.processEntry(v);
            System.out.println("✓ First parking successful: " + ticket1.getTicketId());

            // Second parking attempt (should fail)
            ParkingTicket ticket2 = entryPanel.processEntry(v);
            System.out.println("✗ Test failed! System allowed double parking.");

        } catch (NoAvailableSpotException e) {
            System.out.println("✗ Unexpected error: " + e.getMessage());
        } catch (VehicleAlreadyParkedException e) {
            System.out.println("✓ Test passed! System correctly prevented double parking.");
            System.out.println("Error message: " + e.getMessage());
        }
    }

    private static int getValidIntInput() {
        while (true) {
            try {
                String input = scanner.nextLine().trim();
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.print("Invalid input! Please enter a number: ");
            }
        }
    }

    private static void cleanup() {
        if (scanner != null) {
            scanner.close();
        }
        if (dbManager != null) {
            dbManager.closeConnection();
        }
    }
}
