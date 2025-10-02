package com.checkoutservice.domain.pricing;

public class CouponCalculator extends AbstractPricingCalculator {
    protected void apply(PricingContext ctx){
        if (ctx.coupon != null && !ctx.coupon.isBlank() && ctx.coupon.equalsIgnoreCase("WELCOME10")) {
            ctx.discounts += ctx.subtotal * 0.10;
        }
        ctx.finalizeTotals();
    }
}
