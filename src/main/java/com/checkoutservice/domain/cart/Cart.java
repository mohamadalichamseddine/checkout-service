package com.checkoutservice.domain.cart;

import java.util.ArrayList;
import java.util.List;

public class Cart {
    private final String id;
    private final String currency;
    private final List<CartItem> items = new ArrayList<>();

    public Cart(String id, String currency) { this.id = id; this.currency = currency == null ? "USD" : currency; }

    public String id() { return id; }
    public String currency() { return currency; }
    public List<CartItem> items() { return items; }

    public void add(CartItem item) { items.add(item); }
}
