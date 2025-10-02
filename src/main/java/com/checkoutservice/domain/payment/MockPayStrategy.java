package com.checkoutservice.domain.payment;

public class MockPayStrategy implements PaymentStrategy {
    public boolean pay(double amount){ return true; }
    public String name(){ return "MockPay"; }
}
