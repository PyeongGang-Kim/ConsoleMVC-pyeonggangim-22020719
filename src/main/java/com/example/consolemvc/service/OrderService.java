package com.example.consolemvc.service;

import com.example.consolemvc.exception.OrderNotFoundException;
import com.example.consolemvc.model.Order;
import com.example.consolemvc.model.OrderStatus;
import com.example.consolemvc.repository.OrderRepository;

import java.util.List;

public class OrderService {

    private final OrderRepository repository;

    public OrderService(OrderRepository repository) {
        this.repository = repository;
    }

    public Order createOrder(String customerName, String productName, int quantity) {
        if (customerName == null || customerName.isBlank()) {
            throw new IllegalArgumentException("주문자 이름은 공백일 수 없습니다.");
        }
        if (productName == null || productName.isBlank()) {
            throw new IllegalArgumentException("상품명은 공백일 수 없습니다.");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("수량은 1 이상이어야 합니다.");
        }
        return repository.save(new Order(0L, customerName, productName, quantity));
    }

    public List<Order> getAllOrders() {
        return repository.findAll();
    }

    public Order getOrderById(long id) {
        return repository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
    }

    public Order changeOrderStatus(long id, OrderStatus newStatus) {
        Order order = getOrderById(id);
        order.changeStatus(newStatus);
        repository.update(order);
        return order;
    }

    public Order cancelOrder(long id) {
        return changeOrderStatus(id, OrderStatus.CANCELLED);
    }
}
