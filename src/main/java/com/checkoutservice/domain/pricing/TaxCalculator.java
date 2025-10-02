package com.checkoutservice.domain.pricing;

public class TaxCalculator extends AbstractPricingCalculator {
    private final double rate; // e.g., 0.15 for 15%

    public TaxCalculator(double rate) {
        this.rate = rate;
    }

    @Override
    protected void apply(PricingContext ctx) {
        // tax applied on (subtotal - discounts)
        double taxable = Math.max(0.0, ctx.subtotal - ctx.discounts);
        ctx.tax += taxable * rate;
        ctx.finalizeTotals();
    }
}
