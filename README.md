# Parking Lot Management System

A comprehensive parking lot management system built with Java, supporting both CLI and REST API interfaces.

## 📋 Project Overview

This project implements a smart parking lot management system with:
- **Automatic spot allocation** based on vehicle size
- **Tiered pricing** system
- **Real-time occupancy tracking**
- **JPA Repository pattern** for data persistence
- **REST API** for programmatic access
- **Interactive CLI** for manual operations

## 🌳 Branch Structure

### `master` - JDBC Version (Original)
- Manual JDBC implementation
- DatabaseManager with raw SQL
- Interactive CLI only
- H2 in-memory database

### `spring-boot-jpa-rest` - Spring Boot Version (Current)
- Spring Boot 2.7.18
- Spring Data JPA with repositories
- Both CLI and REST API
- Auto DDL schema generation
- H2 Console integrated

## 🚀 Getting Started (Spring Boot Version)

### Prerequisites
- Java 11 or higher
- Maven 3.6+

### Running the Application

```bash
# Compile
mvn clean compile

# Run Spring Boot application
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## 📡 REST API Endpoints

### Park a Vehicle
```http
POST /api/parking/entry
Content-Type: application/json

{
  "licensePlate": "KA-01-1234",
  "vehicleType": "CAR"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Vehicle parked successfully",
  "ticketId": "TKT-ABC12345",
  "spotId": "F1-M15",
  "floorNumber": 1,
  "entryTime": "2025-11-20T21:00:00"
}
```

### Unpark a Vehicle
```http
POST /api/parking/exit/{ticketId}
```

**Response:**
```json
{
  "success": true,
  "message": "Vehicle exited successfully",
  "ticketId": "TKT-ABC12345",
  "vehicleLicense": "KA-01-1234",
  "entryTime": "2025-11-20T21:00:00",
  "exitTime": "2025-11-20T22:30:00",
  "duration": "2 hour(s)",
  "fee": 30.0,
  "isPaid": true
}
```

### Get Ticket Details
```http
GET /api/parking/ticket/{ticketId}
```

### Get All Active Tickets
```http
GET /api/parking/tickets/active
```

### Get Parking Status
```http
GET /api/parking/status
```

**Response:**
```json
{
  "overallOccupancy": 45.5,
  "activeTickets": 25,
  "totalFloors": 4
}
```

## 💻 Interactive CLI

When you start the application, you'll be prompted:
```
Start Interactive CLI? (y/n):
```

Type `y` to enter interactive mode with the following menu:
```
╔═══════════════════════════════════════╗
║   PARKING LOT MANAGEMENT SYSTEM      ║
╠═══════════════════════════════════════╣
║  1. Park Vehicle                     ║
║  2. Unpark Vehicle                   ║
║  3. Display Status                   ║
║  4. Search Ticket                    ║
║  5. Exit CLI                         ║
╚═══════════════════════════════════════╝
```

## 🗄️ H2 Database Console

Access the H2 console at: `http://localhost:8080/h2-console`

**Connection Settings:**
- **JDBC URL:** `jdbc:h2:mem:parkingdb`
- **Username:** `sa`
- **Password:** *(leave blank)*

### Useful SQL Queries

**View all active tickets:**
```sql
SELECT * FROM PARKING_TICKETS WHERE EXIT_TIME IS NULL;
```

**View parking revenue:**
```sql
SELECT SUM(FEE) as TOTAL_REVENUE, COUNT(*) as TOTAL_TICKETS
FROM PARKING_TICKETS WHERE IS_PAID = TRUE;
```

**Check occupancy:**
```sql
SELECT VEHICLE_TYPE, COUNT(*) as COUNT
FROM PARKING_TICKETS t
JOIN VEHICLES v ON t.VEHICLE_LICENSE = v.LICENSE_PLATE
WHERE t.EXIT_TIME IS NULL
GROUP BY VEHICLE_TYPE;
```

## 🏗️ Architecture

### Technology Stack
- **Backend:** Spring Boot 2.7.18
- **ORM:** Spring Data JPA / Hibernate
- **Database:** H2 (in-memory)
- **Build Tool:** Maven
- **Java Version:** 11

### Package Structure
```
org.example.parking/
├── models/          - JPA entities (Vehicle, ParkingTicket, etc.)
├── repository/      - JPA repositories
├── services/        - Business logic layer
├── controller/      - REST API controllers
├── dto/             - Data Transfer Objects
├── config/          - Spring configuration
├── cli/             - Command Line Interface
├── enums/           - Enumerations
├── exceptions/      - Custom exceptions
├── factory/         - Factory patterns
└── strategies/      - Strategy patterns
```

### Design Patterns
- **Singleton:** ParkingLot management
- **Factory:** Ticket generation
- **Strategy:** Fee calculation, Payment processing
- **Repository:** Data access abstraction

### Key Features
1. **JPA Entities** - Vehicle and ParkingTicket with proper relationships
2. **Repository Interfaces** - Spring Data JPA for CRUD operations
3. **Service Layer** - Business logic separation
4. **REST Controller** - RESTful API endpoints
5. **CLI Runner** - CommandLineRunner for interactive mode

## ⚙️ Configuration

Edit `src/main/resources/application.properties`:

```properties
# Server Port
server.port=8080

# Database
spring.datasource.url=jdbc:h2:mem:parkingdb
spring.jpa.hibernate.ddl-auto=create-drop

# H2 Console
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
```

### Parking Lot Configuration

Modify `ParkingService.initializeParkingLot()` to change:
- Number of floors (default: 4)
- Spots per floor (default: 40 Compact, 30 Medium, 10 Large)

### Pricing Configuration

Modify `TieredPricingStrategy`:
```java
PRICING_CONFIG.put(VehicleType.MOTORCYCLE, new double[]{10.0, 5.0});
PRICING_CONFIG.put(VehicleType.CAR, new double[]{20.0, 10.0});
PRICING_CONFIG.put(VehicleType.BUS, new double[]{50.0, 25.0});
```

## 🧪 Testing with curl

```bash
# Park a car
curl -X POST http://localhost:8080/api/parking/entry \
  -H "Content-Type: application/json" \
  -d '{"licensePlate":"KA-01-1234","vehicleType":"CAR"}'

# Get status
curl http://localhost:8080/api/parking/status

# Exit vehicle (replace TICKET_ID)
curl -X POST http://localhost:8080/api/parking/exit/TKT-ABC12345
```

## 📊 Comparison: JDBC vs Spring Boot Version

| Feature | JDBC Version (master) | Spring Boot Version (spring-boot-jpa-rest) |
|---------|----------------------|-------------------------------------------|
| Database Access | Manual JDBC | Spring Data JPA |
| Repositories | ❌ No | ✅ Yes |
| REST API | ❌ No | ✅ Yes |
| CLI | ✅ Yes | ✅ Yes |
| Auto DDL | ❌ Manual SQL | ✅ Hibernate |
| Transaction Management | ❌ Manual | ✅ @Transactional |
| Dependency Injection | ❌ No | ✅ @Autowired |
| Code Lines | ~1,800 | ~1,400 |

## 🔄 Switching Between Versions

```bash
# Switch to JDBC version
git checkout master
mvn clean compile
mvn exec:java -Dexec.mainClass="org.example.parking.Main"

# Switch to Spring Boot version
git checkout spring-boot-jpa-rest
mvn clean compile
mvn spring-boot:run
```

## 📝 License

This project is created for educational purposes as part of a course assignment.

## 👨‍💻 Author

Jayaharish MR

---

**Note:** This is a course project demonstrating low-level design principles, design patterns, and modern Java development practices with Spring Boot and JPA.
