package com.example.consolemvc.controller;

import com.example.consolemvc.model.Order;
import com.example.consolemvc.model.OrderStatus;
import com.example.consolemvc.repository.InMemoryOrderRepository;
import com.example.consolemvc.service.OrderService;
import com.example.consolemvc.view.OrderView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderControllerTest {

    private OrderService service;
    private ByteArrayOutputStream outContent;

    @BeforeEach
    void setUp() {
        service = new OrderService(new InMemoryOrderRepository());
        outContent = new ByteArrayOutputStream();
    }

    private OrderController buildController(String simulatedInput) {
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));
        System.setOut(new PrintStream(outContent));
        OrderView view = new OrderView();
        return new OrderController(service, view);
    }

    @Test
    @DisplayName("주문 생성 후 주문 목록에 1건 존재")
    void handleCreateOrder_정상_생성() {
        OrderController controller = buildController("홍길동\n노트북\n2\n");
        controller.handleCreateOrder();

        List<Order> orders = service.getAllOrders();
        assertEquals(1, orders.size());
        assertEquals("홍길동", orders.get(0).getCustomerName());
        assertEquals(OrderStatus.PENDING, orders.get(0).getStatus());
    }

    @Test
    @DisplayName("전체 조회 시 빈 상태에서 '등록된 주문이 없습니다' 출력")
    void handleListOrders_빈_목록_메시지() {
        OrderController controller = buildController("");
        controller.handleListOrders();

        assertTrue(outContent.toString().contains("등록된 주문이 없습니다"));
    }

    @Test
    @DisplayName("없는 ID로 단건 조회 시 오류 메시지 출력")
    void handleGetOrder_없는_id_오류메시지() {
        OrderController controller = buildController("999\n");
        controller.handleGetOrder();

        assertTrue(outContent.toString().contains("찾을 수 없습니다") ||
                   outContent.toString().contains("오류") ||
                   outContent.toString().contains("존재하지 않"));
    }

    @Test
    @DisplayName("잘못된 메뉴 입력 시 안내 메시지 출력")
    void handleMenu_잘못된_입력_안내메시지() {
        OrderController controller = buildController("");
        controller.handleMenu("abc");

        assertTrue(outContent.toString().contains("올바른 메뉴"));
    }
}
