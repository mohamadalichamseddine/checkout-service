package com.checkoutservice.domain.payment;

public class CashOnDeliveryStrategy implements PaymentStrategy {
    @Override
    public boolean pay(double amount) {
        // For demo purposes, we "accept" COD immediately.
        // In reality, you'd mark as AwaitingPayment until delivery.
        return true;
    }
    @Override
    public String name() { return "CashOnDelivery"; }
}
