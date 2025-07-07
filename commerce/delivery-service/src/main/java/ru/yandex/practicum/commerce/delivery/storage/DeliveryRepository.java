package ru.yandex.practicum.commerce.delivery.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.commerce.delivery.model.Delivery;

import java.util.Optional;
import java.util.UUID;

public interface DeliveryRepository extends JpaRepository<Delivery, UUID> {
    Optional<Delivery> findDeliveryByOrderId(UUID orderId);
}
