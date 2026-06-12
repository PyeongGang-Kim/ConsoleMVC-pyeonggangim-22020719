package com.example.consolemvc.repository;

import com.example.consolemvc.model.Order;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryOrderRepository implements OrderRepository {

    private final Map<Long, Order> store = new HashMap<>();
    private final AtomicLong idSequence = new AtomicLong(0);

    @Override
    public Order save(Order order) {
        long id = idSequence.incrementAndGet();
        Order saved = new Order(id, order.getCustomerName(), order.getProductName(), order.getQuantity());
        store.put(id, saved);
        return saved;
    }

    @Override
    public Optional<Order> findById(long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Order> findAll() {
        return store.values().stream()
                .sorted((a, b) -> Long.compare(a.getId(), b.getId()))
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public void update(Order order) {
        store.put(order.getId(), order);
    }
}
