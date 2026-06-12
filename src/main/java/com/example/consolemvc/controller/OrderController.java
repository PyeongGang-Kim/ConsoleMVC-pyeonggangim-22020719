package com.example.consolemvc.controller;

import com.example.consolemvc.exception.InvalidOrderStateException;
import com.example.consolemvc.exception.OrderNotFoundException;
import com.example.consolemvc.model.Order;
import com.example.consolemvc.model.OrderStatus;
import com.example.consolemvc.service.OrderService;
import com.example.consolemvc.view.OrderView;

import java.util.List;

public class OrderController {

    private final OrderService service;
    private final OrderView view;

    public OrderController(OrderService service, OrderView view) {
        this.service = service;
        this.view = view;
    }

    public void handleMenu(String input) {
        switch (input) {
            case "1" -> handleCreateOrder();
            case "2" -> handleListOrders();
            case "3" -> handleGetOrder();
            case "4" -> handleChangeStatus();
            case "5" -> handleCancelOrder();
            default  -> view.printMessage("올바른 메뉴를 선택해 주세요.");
        }
    }

    public void handleCreateOrder() {
        String customerName = readNonBlank("주문자 이름: ");
        String productName  = readNonBlank("상품명: ");
        int quantity = readPositiveInt("수량: ");

        try {
            Order order = service.createOrder(customerName, productName, quantity);
            view.printMessage("주문이 생성되었습니다. (ID: " + order.getId() + ")");
        } catch (IllegalArgumentException e) {
            view.printMessage("[오류] " + e.getMessage());
        }
    }

    public void handleListOrders() {
        List<Order> orders = service.getAllOrders();
        view.printOrderList(orders);
    }

    public void handleGetOrder() {
        long id = readLongId();
        try {
            Order order = service.getOrderById(id);
            view.printOrder(order);
        } catch (OrderNotFoundException e) {
            view.printMessage("[오류] " + e.getMessage());
        }
    }

    public void handleChangeStatus() {
        long id = readLongId();
        String statusInput = view.readLine("변경할 상태 (PENDING/CONFIRMED/CANCELLED): ").toUpperCase();
        OrderStatus newStatus;
        try {
            newStatus = OrderStatus.valueOf(statusInput);
        } catch (IllegalArgumentException e) {
            view.printMessage("[오류] 유효하지 않은 상태값입니다: " + statusInput);
            return;
        }
        try {
            Order order = service.changeOrderStatus(id, newStatus);
            view.printMessage("상태가 변경되었습니다: " + order.getStatus());
        } catch (OrderNotFoundException | InvalidOrderStateException e) {
            view.printMessage("[오류] " + e.getMessage());
        }
    }

    public void handleCancelOrder() {
        long id = readLongId();
        try {
            service.cancelOrder(id);
            view.printMessage("주문이 취소되었습니다. (ID: " + id + ")");
        } catch (OrderNotFoundException | InvalidOrderStateException e) {
            view.printMessage("[오류] " + e.getMessage());
        }
    }

    private String readNonBlank(String prompt) {
        while (true) {
            String value = view.readLine(prompt);
            if (!value.isBlank()) return value;
            view.printMessage("[오류] 값을 입력해 주세요.");
        }
    }

    private int readPositiveInt(String prompt) {
        while (true) {
            String raw = view.readLine(prompt);
            try {
                int value = Integer.parseInt(raw);
                if (value > 0) return value;
                view.printMessage("[오류] 1 이상의 숫자를 입력해 주세요.");
            } catch (NumberFormatException e) {
                view.printMessage("[오류] 숫자를 입력해 주세요.");
            }
        }
    }

    private long readLongId() {
        while (true) {
            String raw = view.readLine("주문 ID: ");
            try {
                return Long.parseLong(raw);
            } catch (NumberFormatException e) {
                view.printMessage("[오류] 숫자를 입력해 주세요.");
            }
        }
    }
}
