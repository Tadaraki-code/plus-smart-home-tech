package ru.yandex.practicum.commerce.order.storage;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.commerce.order.model.Order;

import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID>{
    Page<Order> findByUsername(String username, Pageable pageable);
}
