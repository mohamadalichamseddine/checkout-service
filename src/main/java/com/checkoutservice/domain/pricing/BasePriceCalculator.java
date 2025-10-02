package com.checkoutservice.domain.pricing;

import com.checkoutservice.domain.cart.*;

public class BasePriceCalculator extends AbstractPricingCalculator {
    protected void apply(PricingContext ctx){
        double sum = ctx.cart.items().stream()
                .mapToDouble(i -> i.lineTotal().amount())
                .sum();
        ctx.subtotal = sum;
        ctx.finalizeTotals();
    }
}
