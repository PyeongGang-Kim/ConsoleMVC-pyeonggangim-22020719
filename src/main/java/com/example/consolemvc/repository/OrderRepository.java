package com.example.consolemvc.repository;

import com.example.consolemvc.model.Order;

import java.util.List;
import java.util.Optional;

public interface OrderRepository {
    Order save(Order order);
    Optional<Order> findById(long id);
    List<Order> findAll();
    void update(Order order);
}
