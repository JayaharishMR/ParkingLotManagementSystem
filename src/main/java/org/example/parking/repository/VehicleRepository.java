package org.example.parking.repository;

import org.example.parking.models.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, String> {
    // Custom query methods can be added here if needed
    boolean existsByLicensePlate(String licensePlate);
}
