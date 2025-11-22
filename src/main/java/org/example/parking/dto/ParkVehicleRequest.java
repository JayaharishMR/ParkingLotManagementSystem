package org.example.parking.dto;

import org.example.parking.enums.VehicleType;

public class ParkVehicleRequest {
    private String licensePlate;
    private VehicleType vehicleType;

    public ParkVehicleRequest() {
    }

    public ParkVehicleRequest(String licensePlate, VehicleType vehicleType) {
        this.licensePlate = licensePlate;
        this.vehicleType = vehicleType;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public VehicleType getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(VehicleType vehicleType) {
        this.vehicleType = vehicleType;
    }
}
