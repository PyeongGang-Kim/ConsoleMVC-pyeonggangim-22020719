package com.example.consolemvc.model;

import com.example.consolemvc.exception.InvalidOrderStateException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OrderTest {

    private Order createSampleOrder() {
        return new Order(1L, "홍길동", "노트북", 2);
    }

    @Test
    @DisplayName("Order 생성 시 초기 상태는 PENDING")
    void 생성시_초기상태_PENDING() {
        Order order = createSampleOrder();
        assertEquals(OrderStatus.PENDING, order.getStatus());
    }

    @Test
    @DisplayName("Order 생성 시 createdAt이 null이 아님")
    void 생성시_createdAt_not_null() {
        Order order = createSampleOrder();
        assertNotNull(order.getCreatedAt());
    }

    @Test
    @DisplayName("PENDING → CONFIRMED 상태 변경 성공")
    void changeStatus_PENDING_to_CONFIRMED_성공() {
        Order order = createSampleOrder();
        order.changeStatus(OrderStatus.CONFIRMED);
        assertEquals(OrderStatus.CONFIRMED, order.getStatus());
    }

    @Test
    @DisplayName("PENDING → CANCELLED 상태 변경 성공")
    void changeStatus_PENDING_to_CANCELLED_성공() {
        Order order = createSampleOrder();
        order.changeStatus(OrderStatus.CANCELLED);
        assertEquals(OrderStatus.CANCELLED, order.getStatus());
    }

    @Test
    @DisplayName("허용되지 않는 상태 전이 시 InvalidOrderStateException 발생")
    void changeStatus_허용안되는_전이_예외() {
        Order order = createSampleOrder();
        order.changeStatus(OrderStatus.CANCELLED);

        assertThrows(InvalidOrderStateException.class,
                () -> order.changeStatus(OrderStatus.CONFIRMED));
    }

    @Test
    @DisplayName("Order getter가 생성자 인자를 정확히 반환")
    void getter_값_검증() {
        Order order = new Order(1L, "홍길동", "노트북", 2);
        assertAll(
                () -> assertEquals(1L, order.getId()),
                () -> assertEquals("홍길동", order.getCustomerName()),
                () -> assertEquals("노트북", order.getProductName()),
                () -> assertEquals(2, order.getQuantity())
        );
    }
}
