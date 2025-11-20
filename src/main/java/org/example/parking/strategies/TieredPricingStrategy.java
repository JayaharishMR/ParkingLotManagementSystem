package org.example.parking.strategies;

import org.example.parking.enums.VehicleType;
import org.example.parking.models.ParkingTicket;

import java.util.HashMap;
import java.util.Map;

public class TieredPricingStrategy implements FeeCalculationStrategy {

    // Pricing configuration: Vehicle Type -> [First Hour Rate, Subsequent Hour Rate]
    private static final Map<VehicleType, double[]> PRICING_CONFIG = new HashMap<>();

    static {
        // Format: {firstHourRate, subsequentHourRate}
        PRICING_CONFIG.put(VehicleType.MOTORCYCLE, new double[]{10.0, 5.0});
        PRICING_CONFIG.put(VehicleType.CAR, new double[]{20.0, 10.0});
        PRICING_CONFIG.put(VehicleType.BUS, new double[]{50.0, 25.0});
    }

    @Override
    public double calculateFee(ParkingTicket ticket) {
        VehicleType vehicleType = ticket.getVehicle().getVehicleType();
        long hours = ticket.getParkingDurationInHours();

        double[] rates = PRICING_CONFIG.get(vehicleType);
        double firstHourRate = rates[0];
        double subsequentHourRate = rates[1];

        // Calculate fee: first hour + (remaining hours * subsequent rate)
        double fee = firstHourRate;
        if (hours > 1) {
            fee += (hours - 1) * subsequentHourRate;
        }

        return fee;
    }

    public static double getFirstHourRate(VehicleType vehicleType) {
        return PRICING_CONFIG.get(vehicleType)[0];
    }

    public static double getSubsequentHourRate(VehicleType vehicleType) {
        return PRICING_CONFIG.get(vehicleType)[1];
    }
}
