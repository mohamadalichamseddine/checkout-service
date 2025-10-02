package com.checkoutservice.domain.pricing;

public class ShippingCalculator extends AbstractPricingCalculator {
    private final double shipping;
    private final double freeThreshold;

    public ShippingCalculator(double shipping, double freeThreshold) {
        this.shipping = shipping;
        this.freeThreshold = freeThreshold;
    }

    @Override
    protected void apply(PricingContext ctx) {
        // if total (before shipping) >= freeThreshold => free shipping
        double beforeShipping = Math.max(0.0, ctx.subtotal - ctx.discounts + ctx.tax);
        if (beforeShipping >= freeThreshold) {
            // free shipping
        } else {
            ctx.shipping += shipping;
        }
        ctx.finalizeTotals();
    }
}
