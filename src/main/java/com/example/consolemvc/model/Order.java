package com.example.consolemvc.model;

import com.example.consolemvc.exception.InvalidOrderStateException;

import java.time.LocalDateTime;

public class Order {
    private final long id;
    private final String customerName;
    private final String productName;
    private final int quantity;
    private OrderStatus status;
    private final LocalDateTime createdAt;

    public Order(long id, String customerName, String productName, int quantity) {
        this.id = id;
        this.customerName = customerName;
        this.productName = productName;
        this.quantity = quantity;
        this.status = OrderStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    public void changeStatus(OrderStatus newStatus) {
        if (!this.status.canTransitionTo(newStatus)) {
            throw new InvalidOrderStateException(
                    this.status + " → " + newStatus + " 전이는 허용되지 않습니다.");
        }
        this.status = newStatus;
    }

    public long getId() { return id; }
    public String getCustomerName() { return customerName; }
    public String getProductName() { return productName; }
    public int getQuantity() { return quantity; }
    public OrderStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
