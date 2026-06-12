package com.example.consolemvc.exception;

public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(long id) {
        super("주문을 찾을 수 없습니다. ID: " + id);
    }
}
