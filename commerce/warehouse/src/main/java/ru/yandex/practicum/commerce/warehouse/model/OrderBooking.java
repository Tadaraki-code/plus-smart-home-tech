package ru.yandex.practicum.commerce.warehouse.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Data
@Builder
@Table(name = "order_booking")
@NoArgsConstructor
@AllArgsConstructor
public class OrderBooking {
    @Id
    private UUID orderId;
    @Column(name = "product_id", nullable = false)
    private UUID productId;
    @Column(name = "delivery_id")
    private UUID deliveryId;
    @Column(name = "booked_quantity", nullable = false)
    private int bookedQuantity;
}
