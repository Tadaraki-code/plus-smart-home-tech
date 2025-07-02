package ru.yandex.practicum.commerce.warehouse.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.commerce.warehouse.model.WarehouseItem;

import java.util.UUID;

@Repository
public interface WarehouseItemRepository extends JpaRepository<WarehouseItem, UUID> {
}
