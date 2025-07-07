package ru.yandex.practicum.commerce.delivery.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.commerce.delivery.model.WarehouseAddress;

import java.util.UUID;

public interface WarehouseAddressRepository extends JpaRepository<WarehouseAddress, UUID> {
}
