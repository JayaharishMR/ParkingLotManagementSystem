package org.example.parking.controller;

import org.example.parking.dto.ParkVehicleRequest;
import org.example.parking.dto.ParkingStatusResponse;
import org.example.parking.enums.VehicleType;
import org.example.parking.exceptions.*;
import org.example.parking.models.ParkingTicket;
import org.example.parking.services.ParkingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/parking")
public class ParkingController {

    @Autowired
    private ParkingService parkingService;

    @PostMapping("/entry")
    public ResponseEntity<?> parkVehicle(@RequestBody ParkVehicleRequest request) {
        try {
            ParkingTicket ticket = parkingService.parkVehicle(
                    request.getLicensePlate(),
                    request.getVehicleType()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Vehicle parked successfully");
            response.put("ticketId", ticket.getTicketId());
            response.put("spotId", ticket.getSpotId());
            response.put("floorNumber", ticket.getFloorNumber());
            response.put("entryTime", ticket.getEntryTime());

            return ResponseEntity.ok(response);

        } catch (NoAvailableSpotException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(createErrorResponse(e.getMessage()));
        } catch (VehicleAlreadyParkedException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Internal server error: " + e.getMessage()));
        }
    }

    @PostMapping("/exit/{ticketId}")
    public ResponseEntity<?> unparkVehicle(@PathVariable String ticketId) {
        try {
            ParkingTicket ticket = parkingService.unparkVehicle(ticketId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Vehicle exited successfully");
            response.put("ticketId", ticket.getTicketId());
            response.put("vehicleLicense", ticket.getVehicle().getLicensePlate());
            response.put("entryTime", ticket.getEntryTime());
            response.put("exitTime", ticket.getExitTime());
            response.put("duration", ticket.getParkingDurationInHours() + " hour(s)");
            response.put("fee", ticket.getFee());
            response.put("isPaid", ticket.isPaid());

            return ResponseEntity.ok(response);

        } catch (InvalidTicketException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
        } catch (TicketAlreadyProcessedException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Internal server error: " + e.getMessage()));
        }
    }

    @GetMapping("/ticket/{ticketId}")
    public ResponseEntity<?> getTicket(@PathVariable String ticketId) {
        try {
            ParkingTicket ticket = parkingService.getTicket(ticketId);
            return ResponseEntity.ok(ticket);
        } catch (InvalidTicketException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/tickets/active")
    public ResponseEntity<List<ParkingTicket>> getActiveTickets() {
        return ResponseEntity.ok(parkingService.getAllActiveTickets());
    }

    @GetMapping("/tickets/all")
    public ResponseEntity<List<ParkingTicket>> getAllTickets() {
        return ResponseEntity.ok(parkingService.getAllTickets());
    }

    @GetMapping("/status")
    public ResponseEntity<ParkingStatusResponse> getParkingStatus() {
        ParkingStatusResponse response = new ParkingStatusResponse();
        response.setOverallOccupancy(parkingService.getOverallOccupancy());
        response.setActiveTickets(parkingService.getAllActiveTickets().size());
        response.setTotalFloors(parkingService.getParkingLot().getFloors().size());

        return ResponseEntity.ok(response);
    }

    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", message);
        return response;
    }
}
