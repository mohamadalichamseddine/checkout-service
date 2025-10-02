package com.checkoutservice.domain.pricing;

import com.checkoutservice.domain.cart.*;

public class PricingContext {
    public final Cart cart;
    public final String coupon;

    public double subtotal;
    public double discounts;
    public double tax;
    public double shipping;
    public double total;

    public PricingContext(Cart cart, String coupon){
        this.cart = cart;
        this.coupon = coupon;
        this.subtotal = 0.0;
        this.discounts = 0.0;
        this.tax = 0.0;
        this.shipping = 0.0;
        this.total = 0.0;
    }

    public void finalizeTotals(){
        this.total = Math.max(0.0, subtotal - discounts) + tax + shipping;
    }
}
