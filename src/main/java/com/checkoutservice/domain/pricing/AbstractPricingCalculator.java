package com.checkoutservice.domain.pricing;

public abstract class AbstractPricingCalculator implements PricingCalculator {
    protected PricingCalculator next;

    public PricingCalculator setNext(PricingCalculator n){
        this.next = n;
        return n;
    }

    public PricingContext calculate(PricingContext ctx){
        apply(ctx);
        return (next == null) ? ctx : next.calculate(ctx);
    }

    protected abstract void apply(PricingContext ctx);
}
