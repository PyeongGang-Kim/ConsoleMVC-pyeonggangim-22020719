package com.example.consolemvc.service;

import com.example.consolemvc.exception.InvalidOrderStateException;
import com.example.consolemvc.exception.OrderNotFoundException;
import com.example.consolemvc.model.Order;
import com.example.consolemvc.model.OrderStatus;
import com.example.consolemvc.repository.InMemoryOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OrderServiceTest {

    private OrderService service;

    @BeforeEach
    void setUp() {
        service = new OrderService(new InMemoryOrderRepository());
    }

    @Test
    @DisplayName("주문 생성 시 초기 상태는 PENDING")
    void createOrder_정상_생성() {
        Order order = service.createOrder("홍길동", "노트북", 1);
        assertEquals(OrderStatus.PENDING, order.getStatus());
        assertTrue(order.getId() > 0);
    }

    @Test
    @DisplayName("수량이 0 이하이면 IllegalArgumentException 발생")
    void createOrder_수량_0_이하_예외() {
        assertThrows(IllegalArgumentException.class,
                () -> service.createOrder("홍길동", "노트북", 0));
    }

    @Test
    @DisplayName("주문자 이름이 공백이면 IllegalArgumentException 발생")
    void createOrder_이름_공백_예외() {
        assertThrows(IllegalArgumentException.class,
                () -> service.createOrder("   ", "노트북", 1));
    }

    @Test
    @DisplayName("존재하지 않는 ID 조회 시 OrderNotFoundException 발생")
    void getOrderById_없는_id_예외() {
        assertThrows(OrderNotFoundException.class,
                () -> service.getOrderById(999L));
    }

    @Test
    @DisplayName("PENDING → CONFIRMED 상태 변경 성공")
    void changeStatus_PENDING_to_CONFIRMED() {
        Order order = service.createOrder("홍길동", "노트북", 1);
        Order updated = service.changeOrderStatus(order.getId(), OrderStatus.CONFIRMED);
        assertEquals(OrderStatus.CONFIRMED, updated.getStatus());
    }

    @Test
    @DisplayName("CANCELLED → CONFIRMED 전이 시 InvalidOrderStateException 발생")
    void changeStatus_CANCELLED_to_CONFIRMED_예외() {
        Order order = service.createOrder("홍길동", "노트북", 1);
        service.changeOrderStatus(order.getId(), OrderStatus.CANCELLED);

        assertThrows(InvalidOrderStateException.class,
                () -> service.changeOrderStatus(order.getId(), OrderStatus.CONFIRMED));
    }

    @Test
    @DisplayName("이미 취소된 주문을 cancelOrder 시 InvalidOrderStateException 발생")
    void cancelOrder_이미_취소된_주문_예외() {
        Order order = service.createOrder("홍길동", "노트북", 1);
        service.cancelOrder(order.getId());

        assertThrows(InvalidOrderStateException.class,
                () -> service.cancelOrder(order.getId()));
    }
}
