package org.example.parking.config;

import org.example.parking.strategies.CashPaymentStrategy;
import org.example.parking.strategies.FeeCalculationStrategy;
import org.example.parking.strategies.PaymentStrategy;
import org.example.parking.strategies.TieredPricingStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ParkingConfiguration {

    @Bean
    public FeeCalculationStrategy feeCalculationStrategy() {
        return new TieredPricingStrategy();
    }

    @Bean
    public PaymentStrategy paymentStrategy() {
        return new CashPaymentStrategy();
    }
}
