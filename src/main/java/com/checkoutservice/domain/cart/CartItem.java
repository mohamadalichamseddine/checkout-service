package com.checkoutservice.domain.cart;

public record CartItem(String productId, int qty, Money unitPrice) {
    public Money lineTotal() { return new Money(unitPrice.amount() * qty, unitPrice.currency()); }
}
