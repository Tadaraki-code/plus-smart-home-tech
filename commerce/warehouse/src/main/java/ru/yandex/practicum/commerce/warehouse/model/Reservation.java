package ru.yandex.practicum.commerce.warehouse.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "warehouse_item_reserved")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Column(name = "shopping_cart_id", nullable = false)
    UUID shoppingCartId;
    @Column(name = "product_id", nullable = false)
    UUID productId;
    @Column(name = "reserved_quantity", nullable = false)
    long reservedQuantity;
}
