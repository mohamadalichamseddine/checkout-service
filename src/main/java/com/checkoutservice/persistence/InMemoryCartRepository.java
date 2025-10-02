package com.checkoutservice.persistence;

import com.checkoutservice.domain.cart.*;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class InMemoryCartRepository implements CartRepository {
    private final Map<String, Cart> db = new HashMap<>();

    public String create(String currency){
        String id = UUID.randomUUID().toString();
        db.put(id, new Cart(id, currency));
        return id;
    }

    public Cart get(String id){
        Cart c = db.get(id);
        if (c == null) throw new NoSuchElementException("Cart not found: " + id);
        return c;
    }

    public Cart addItem(String id, CartItem item){
        Cart c = get(id);
        c.add(item);
        return c;
    }
}
