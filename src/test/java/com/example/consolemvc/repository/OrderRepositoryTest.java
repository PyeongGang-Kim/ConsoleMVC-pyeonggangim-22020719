package com.example.consolemvc.repository;

import com.example.consolemvc.model.Order;
import com.example.consolemvc.model.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class OrderRepositoryTest {

    private OrderRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryOrderRepository();
    }

    @Test
    @DisplayName("save 후 반환된 Order에 id가 부여됨")
    void save_저장후_id_부여() {
        Order order = new Order(0L, "홍길동", "노트북", 1);
        Order saved = repository.save(order);
        assertTrue(saved.getId() > 0);
    }

    @Test
    @DisplayName("저장한 Order를 findById로 조회 가능")
    void findById_존재하는_id() {
        Order saved = repository.save(new Order(0L, "홍길동", "노트북", 1));
        Optional<Order> found = repository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals(saved.getId(), found.get().getId());
    }

    @Test
    @DisplayName("존재하지 않는 id 조회 시 Optional.empty() 반환")
    void findById_없는_id() {
        Optional<Order> found = repository.findById(999L);
        assertTrue(found.isEmpty());
    }

    @Test
    @DisplayName("findAll은 ID 오름차순으로 정렬된 목록 반환")
    void findAll_정렬순서() {
        repository.save(new Order(0L, "홍길동", "노트북", 1));
        repository.save(new Order(0L, "김영희", "마우스", 2));
        repository.save(new Order(0L, "이철수", "키보드", 3));

        List<Order> orders = repository.findAll();
        assertEquals(3, orders.size());
        assertTrue(orders.get(0).getId() < orders.get(1).getId());
        assertTrue(orders.get(1).getId() < orders.get(2).getId());
    }

    @Test
    @DisplayName("update 후 findById로 변경된 상태 확인")
    void update_상태변경_반영() {
        Order saved = repository.save(new Order(0L, "홍길동", "노트북", 1));
        saved.changeStatus(OrderStatus.CONFIRMED);
        repository.update(saved);

        Order found = repository.findById(saved.getId()).get();
        assertEquals(OrderStatus.CONFIRMED, found.getStatus());
    }
}
