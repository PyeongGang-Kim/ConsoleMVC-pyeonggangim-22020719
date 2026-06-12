package com.example.consolemvc.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OrderStatusTest {

    @Test
    @DisplayName("PENDING → CONFIRMED 전이 허용")
    void canTransitionTo_PENDING_to_CONFIRMED_허용() {
        assertTrue(OrderStatus.PENDING.canTransitionTo(OrderStatus.CONFIRMED));
    }

    @Test
    @DisplayName("PENDING → CANCELLED 전이 허용")
    void canTransitionTo_PENDING_to_CANCELLED_허용() {
        assertTrue(OrderStatus.PENDING.canTransitionTo(OrderStatus.CANCELLED));
    }

    @Test
    @DisplayName("CONFIRMED → CANCELLED 전이 허용")
    void canTransitionTo_CONFIRMED_to_CANCELLED_허용() {
        assertTrue(OrderStatus.CONFIRMED.canTransitionTo(OrderStatus.CANCELLED));
    }

    @Test
    @DisplayName("CONFIRMED → PENDING 전이 불가")
    void canTransitionTo_CONFIRMED_to_PENDING_불가() {
        assertFalse(OrderStatus.CONFIRMED.canTransitionTo(OrderStatus.PENDING));
    }

    @Test
    @DisplayName("CANCELLED → CONFIRMED 전이 불가")
    void canTransitionTo_CANCELLED_to_CONFIRMED_불가() {
        assertFalse(OrderStatus.CANCELLED.canTransitionTo(OrderStatus.CONFIRMED));
    }

    @Test
    @DisplayName("CANCELLED → PENDING 전이 불가")
    void canTransitionTo_CANCELLED_to_PENDING_불가() {
        assertFalse(OrderStatus.CANCELLED.canTransitionTo(OrderStatus.PENDING));
    }
}
