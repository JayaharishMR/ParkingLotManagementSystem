package org.example.parking.services;

import org.example.parking.enums.PaymentMethod;
import org.example.parking.models.ParkingTicket;
import org.example.parking.strategies.FeeCalculationStrategy;
import org.example.parking.strategies.PaymentStrategy;

public class PaymentProcessor {
    private FeeCalculationStrategy feeCalculationStrategy;
    private PaymentStrategy paymentStrategy;

    public PaymentProcessor(FeeCalculationStrategy feeCalculationStrategy, PaymentStrategy paymentStrategy) {
        this.feeCalculationStrategy = feeCalculationStrategy;
        this.paymentStrategy = paymentStrategy;
    }

    public double calculateFee(ParkingTicket ticket) {
        double fee = feeCalculationStrategy.calculateFee(ticket);
        ticket.setFee(fee);
        return fee;
    }

    public boolean processPayment(ParkingTicket ticket, PaymentMethod method) {
        double fee = ticket.getFee();

        if (fee == 0) {
            // Calculate fee if not already calculated
            fee = calculateFee(ticket);
        }

        boolean success = paymentStrategy.processPayment(fee, method);

        if (success) {
            ticket.markAsPaid();
            String receipt = paymentStrategy.getPaymentReceipt(fee, method);
            System.out.println(receipt);
        }

        return success;
    }

    public void setFeeCalculationStrategy(FeeCalculationStrategy feeCalculationStrategy) {
        this.feeCalculationStrategy = feeCalculationStrategy;
    }

    public void setPaymentStrategy(PaymentStrategy paymentStrategy) {
        this.paymentStrategy = paymentStrategy;
    }
}
