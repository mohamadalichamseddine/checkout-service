package com.checkoutservice.persistence;

import com.checkoutservice.domain.order.*;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class InMemoryOrderRepository implements OrderRepository {
    private final Map<String, Order> db = new HashMap<>();

    public void save(Order o){ db.put(o.getId(), o); }

    public Order get(String id){
        Order o = db.get(id);
        if (o == null) throw new NoSuchElementException("Order not found: " + id);
        return o;
    }
}
