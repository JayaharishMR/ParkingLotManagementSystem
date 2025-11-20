package org.example.parking.strategies;

import org.example.parking.enums.PaymentMethod;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CashPaymentStrategy implements PaymentStrategy {

    @Override
    public boolean processPayment(double amount, PaymentMethod method) {
        if (method != PaymentMethod.CASH) {
            System.out.println("This payment method is not supported yet: " + method);
            return false;
        }

        // Simulate cash payment processing
        System.out.println("Processing cash payment of Rs. " + amount);
        return true;
    }

    @Override
    public String getPaymentReceipt(double amount, PaymentMethod method) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String timestamp = LocalDateTime.now().format(formatter);

        return "\n========== PAYMENT RECEIPT ==========\n" +
               "Payment Method: " + method + "\n" +
               "Amount Paid: Rs. " + String.format("%.2f", amount) + "\n" +
               "Timestamp: " + timestamp + "\n" +
               "Status: SUCCESS\n" +
               "====================================\n";
    }
}
