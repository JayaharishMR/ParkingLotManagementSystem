package org.example.parking.repository;

import org.example.parking.models.ParkingTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParkingTicketRepository extends JpaRepository<ParkingTicket, String> {

    // Find all active tickets (not exited yet)
    @Query("SELECT t FROM ParkingTicket t WHERE t.exitTime IS NULL")
    List<ParkingTicket> findAllActiveTickets();

    // Find ticket by vehicle license plate
    Optional<ParkingTicket> findByVehicleLicensePlateAndExitTimeIsNull(String licensePlate);

    // Check if vehicle is currently parked
    boolean existsByVehicleLicensePlateAndExitTimeIsNull(String licensePlate);

    // Find all paid tickets
    List<ParkingTicket> findAllByIsPaidTrue();

    // Find all unpaid tickets
    List<ParkingTicket> findAllByIsPaidFalse();
}
