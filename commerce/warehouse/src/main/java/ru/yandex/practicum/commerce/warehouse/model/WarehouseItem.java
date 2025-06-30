package ru.yandex.practicum.commerce.warehouse.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "warehouse_item", schema = "warehouse_schema")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WarehouseItem {

    @Id
    UUID id;
    @Column(name = "fragile", nullable = false)
    boolean fragile;
    @Column(name = "width", nullable = false)
    double width;
    @Column(name = "height", nullable = false)
    double height;
    @Column(name = "depth", nullable = false)
    double depth;
    @Column(name = "weight", nullable = false)
    double weight;
    @Column(name = "quantity", nullable = false)
    long quantity;
    @Column(name = "reserved_quantity", nullable = false)
    private long reservedQuantity;

}
