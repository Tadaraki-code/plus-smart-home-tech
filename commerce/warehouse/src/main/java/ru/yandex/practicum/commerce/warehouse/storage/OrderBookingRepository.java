package ru.yandex.practicum.commerce.warehouse.storage;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.commerce.warehouse.model.OrderBooking;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderBookingRepository extends JpaRepository<OrderBooking, UUID> {
    List<OrderBooking> findByOrderId(UUID orderId);
}
