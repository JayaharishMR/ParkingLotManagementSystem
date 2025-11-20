package org.example.parking.cli;

import org.example.parking.enums.VehicleType;
import org.example.parking.exceptions.*;
import org.example.parking.models.ParkingTicket;
import org.example.parking.services.ParkingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Scanner;

@Component
public class ParkingCLIRunner implements CommandLineRunner {

    @Autowired
    private ParkingService parkingService;

    private Scanner scanner;

    @Override
    public void run(String... args) {
        scanner = new Scanner(System.in);

        System.out.println("\n╔═══════════════════════════════════════════════════════════════╗");
        System.out.println("║        PARKING LOT MANAGEMENT SYSTEM - Spring Boot           ║");
        System.out.println("║                                                               ║");
        System.out.println("║  REST API running on: http://localhost:8080                  ║");
        System.out.println("║  H2 Console: http://localhost:8080/h2-console                ║");
        System.out.println("║  JDBC URL: jdbc:h2:mem:parkingdb                             ║");
        System.out.println("╚═══════════════════════════════════════════════════════════════╝\n");

        // Ask if user wants CLI or just REST API
        System.out.print("Start Interactive CLI? (y/n): ");
        String response = scanner.nextLine().trim().toLowerCase();

        if (!"y".equals(response)) {
            System.out.println("\nCLI mode disabled. REST API is running...");
            System.out.println("Press Ctrl+C to stop the application.\n");
            return;
        }

        runInteractiveCLI();
    }

    private void runInteractiveCLI() {
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
                        System.out.println("\nExiting CLI. REST API continues running...");
                        return;
                    default:
                        System.out.println("Invalid choice!");
                }
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }

            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
        }
    }

    private void displayMainMenu() {
        System.out.println("\n╔═══════════════════════════════════════╗");
        System.out.println("║   PARKING LOT MANAGEMENT SYSTEM      ║");
        System.out.println("╠═══════════════════════════════════════╣");
        System.out.println("║  1. Park Vehicle                     ║");
        System.out.println("║  2. Unpark Vehicle                   ║");
        System.out.println("║  3. Display Status                   ║");
        System.out.println("║  4. Search Ticket                    ║");
        System.out.println("║  5. Exit CLI                         ║");
        System.out.println("╚═══════════════════════════════════════╝");
        System.out.print("Enter choice: ");
    }

    private void parkVehicle() {
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

        try {
            ParkingTicket ticket = parkingService.parkVehicle(licensePlate, vehicleType);
            System.out.println("\n✓ SUCCESS! Vehicle parked.");
            System.out.println("Ticket ID: " + ticket.getTicketId());
            System.out.println("Spot: " + ticket.getSpotId() + " (Floor " + ticket.getFloorNumber() + ")");
            System.out.println("Entry Time: " + ticket.getEntryTime());
        } catch (NoAvailableSpotException e) {
            System.out.println("\n✗ ERROR: " + e.getMessage());
        } catch (VehicleAlreadyParkedException e) {
            System.out.println("\n✗ ERROR: " + e.getMessage());
        }
    }

    private void unparkVehicle() {
        System.out.println("\n--- Unpark Vehicle ---");
        System.out.print("Enter Ticket ID: ");
        String ticketId = scanner.nextLine().trim().toUpperCase();

        if (ticketId.isEmpty()) {
            System.out.println("Ticket ID cannot be empty!");
            return;
        }

        try {
            ParkingTicket ticket = parkingService.unparkVehicle(ticketId);
            System.out.println("\n✓ SUCCESS! Vehicle exited.");
            System.out.println("Vehicle: " + ticket.getVehicle().getLicensePlate());
            System.out.println("Duration: " + ticket.getParkingDurationInHours() + " hour(s)");
            System.out.println("Total Fee: Rs. " + ticket.getFee());
        } catch (InvalidTicketException e) {
            System.out.println("\n✗ ERROR: " + e.getMessage());
        } catch (TicketAlreadyProcessedException e) {
            System.out.println("\n✗ ERROR: " + e.getMessage());
        }
    }

    private void displayStatus() {
        System.out.println("\n========== PARKING LOT STATUS ==========");
        System.out.println("Overall Occupancy: " + String.format("%.1f%%", parkingService.getOverallOccupancy()));
        System.out.println("Active Tickets: " + parkingService.getAllActiveTickets().size());
        System.out.println("========================================");
    }

    private void searchTicket() {
        System.out.println("\n--- Search Ticket ---");
        System.out.print("Enter Ticket ID: ");
        String ticketId = scanner.nextLine().trim().toUpperCase();

        try {
            ParkingTicket ticket = parkingService.getTicket(ticketId);
            System.out.println("\n✓ Ticket Found:");
            System.out.println("Ticket ID: " + ticket.getTicketId());
            System.out.println("Vehicle: " + ticket.getVehicle().getLicensePlate() +
                    " (" + ticket.getVehicle().getVehicleType() + ")");
            System.out.println("Spot: " + ticket.getSpotId() + " (Floor " + ticket.getFloorNumber() + ")");
            System.out.println("Entry Time: " + ticket.getEntryTime());
            if (ticket.getExitTime() != null) {
                System.out.println("Exit Time: " + ticket.getExitTime());
                System.out.println("Fee: Rs. " + ticket.getFee());
            } else {
                System.out.println("Status: Currently Parked");
                System.out.println("Duration: " + ticket.getParkingDurationInHours() + " hour(s)");
            }
        } catch (InvalidTicketException e) {
            System.out.println("✗ Ticket not found!");
        }
    }

    private int getValidIntInput() {
        while (true) {
            try {
                String input = scanner.nextLine().trim();
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.print("Invalid input! Please enter a number: ");
            }
        }
    }
}
