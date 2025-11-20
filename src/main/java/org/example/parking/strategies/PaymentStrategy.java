package org.example.parking.strategies;

import org.example.parking.enums.PaymentMethod;

public interface PaymentStrategy {
    boolean processPayment(double amount, PaymentMethod method);
    String getPaymentReceipt(double amount, PaymentMethod method);
}
