package com.checkoutservice.domain.payment;

public class FailPayStrategy implements PaymentStrategy {
    public boolean pay(double amount){ return false; }
    public String name(){ return "FailPay"; }
}
