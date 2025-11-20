package org.example.parking.dao;

import org.example.parking.exceptions.DatabaseException;
import org.h2.tools.Server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:h2:mem:parkingdb;DB_CLOSE_DELAY=-1";
    private static final String DB_USER = "sa";
    private static final String DB_PASSWORD = "";

    private Connection connection;
    private Server webServer;

    public DatabaseManager() {
        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("H2 Driver not found: " + e.getMessage());
        }
    }

    public void startH2Console() {
        try {
            webServer = Server.createWebServer("-web", "-webAllowOthers", "-webPort", "8082");
            webServer.start();
            System.out.println("\n╔════════════════════════════════════════════════════════════╗");
            System.out.println("║  H2 Database Console Started!                             ║");
            System.out.println("║  URL: http://localhost:8082                               ║");
            System.out.println("║                                                            ║");
            System.out.println("║  Connection Details:                                       ║");
            System.out.println("║  JDBC URL: jdbc:h2:mem:parkingdb                          ║");
            System.out.println("║  Username: sa                                              ║");
            System.out.println("║  Password: (leave blank)                                   ║");
            System.out.println("╚════════════════════════════════════════════════════════════╝\n");
        } catch (SQLException e) {
            System.err.println("Failed to start H2 console: " + e.getMessage());
        }
    }

    public Connection getConnection() throws DatabaseException {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            }
            return connection;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to get database connection", e);
        }
    }

    public void initializeSchema() throws DatabaseException {
        System.out.println("Initializing database schema...");

        String createVehiclesTable = "CREATE TABLE IF NOT EXISTS vehicles (" +
                "license_plate VARCHAR(50) PRIMARY KEY, " +
                "vehicle_type VARCHAR(20) NOT NULL" +
                ")";

        String createParkingFloorsTable = "CREATE TABLE IF NOT EXISTS parking_floors (" +
                "floor_number INT PRIMARY KEY, " +
                "total_spots INT NOT NULL, " +
                "occupied_spots INT DEFAULT 0" +
                ")";

        String createParkingSpotsTable = "CREATE TABLE IF NOT EXISTS parking_spots (" +
                "spot_id VARCHAR(50) PRIMARY KEY, " +
                "spot_type VARCHAR(20) NOT NULL, " +
                "spot_status VARCHAR(20) NOT NULL, " +
                "floor_number INT NOT NULL, " +
                "distance_from_entrance INT NOT NULL, " +
                "vehicle_license VARCHAR(50), " +
                "last_occupied_time TIMESTAMP, " +
                "FOREIGN KEY (floor_number) REFERENCES parking_floors(floor_number), " +
                "FOREIGN KEY (vehicle_license) REFERENCES vehicles(license_plate)" +
                ")";

        String createParkingTicketsTable = "CREATE TABLE IF NOT EXISTS parking_tickets (" +
                "ticket_id VARCHAR(50) PRIMARY KEY, " +
                "vehicle_license VARCHAR(50) NOT NULL, " +
                "spot_id VARCHAR(50) NOT NULL, " +
                "floor_number INT NOT NULL, " +
                "entry_time TIMESTAMP NOT NULL, " +
                "exit_time TIMESTAMP, " +
                "fee DECIMAL(10, 2) DEFAULT 0.0, " +
                "is_paid BOOLEAN DEFAULT FALSE, " +
                "FOREIGN KEY (vehicle_license) REFERENCES vehicles(license_plate), " +
                "FOREIGN KEY (spot_id) REFERENCES parking_spots(spot_id)" +
                ")";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(createVehiclesTable);
            stmt.execute(createParkingFloorsTable);
            stmt.execute(createParkingSpotsTable);
            stmt.execute(createParkingTicketsTable);

            System.out.println("Database schema initialized successfully!");

        } catch (SQLException e) {
            throw new DatabaseException("Failed to initialize database schema", e);
        }
    }

    // DAO Methods for persisting data

    public void saveVehicle(String licensePlate, String vehicleType) throws DatabaseException {
        String sql = "MERGE INTO vehicles (license_plate, vehicle_type) VALUES (?, ?)";

        try (Connection conn = getConnection();
             java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, licensePlate);
            stmt.setString(2, vehicleType);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new DatabaseException("Failed to save vehicle", e);
        }
    }

    public void saveParkingTicket(String ticketId, String vehicleLicense, String spotId,
                                  int floorNumber, java.time.LocalDateTime entryTime,
                                  double fee, boolean isPaid) throws DatabaseException {
        String sql = "INSERT INTO parking_tickets (ticket_id, vehicle_license, spot_id, floor_number, " +
                     "entry_time, fee, is_paid) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, ticketId);
            stmt.setString(2, vehicleLicense);
            stmt.setString(3, spotId);
            stmt.setInt(4, floorNumber);
            stmt.setTimestamp(5, java.sql.Timestamp.valueOf(entryTime));
            stmt.setDouble(6, fee);
            stmt.setBoolean(7, isPaid);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new DatabaseException("Failed to save parking ticket", e);
        }
    }

    public void updateParkingTicket(String ticketId, java.time.LocalDateTime exitTime,
                                    double fee, boolean isPaid) throws DatabaseException {
        String sql = "UPDATE parking_tickets SET exit_time = ?, fee = ?, is_paid = ? WHERE ticket_id = ?";

        try (Connection conn = getConnection();
             java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setTimestamp(1, java.sql.Timestamp.valueOf(exitTime));
            stmt.setDouble(2, fee);
            stmt.setBoolean(3, isPaid);
            stmt.setString(4, ticketId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new DatabaseException("Failed to update parking ticket", e);
        }
    }

    public void updateParkingSpot(String spotId, String status, String vehicleLicense,
                                  java.time.LocalDateTime lastOccupiedTime) throws DatabaseException {
        String sql = "UPDATE parking_spots SET spot_status = ?, vehicle_license = ?, last_occupied_time = ? " +
                     "WHERE spot_id = ?";

        try (Connection conn = getConnection();
             java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status);
            stmt.setString(2, vehicleLicense);
            stmt.setTimestamp(3, lastOccupiedTime != null ? java.sql.Timestamp.valueOf(lastOccupiedTime) : null);
            stmt.setString(4, spotId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new DatabaseException("Failed to update parking spot", e);
        }
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed.");
            }
            if (webServer != null) {
                webServer.stop();
                System.out.println("H2 Console stopped.");
            }
        } catch (SQLException e) {
            System.err.println("Error closing database connection: " + e.getMessage());
        }
    }
}
