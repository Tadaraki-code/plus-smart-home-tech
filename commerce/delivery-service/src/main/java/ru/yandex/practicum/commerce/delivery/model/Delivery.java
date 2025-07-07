package ru.yandex.practicum.commerce.delivery.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.yandex.practicum.commerce.interactionapi.delivery.DeliveryState;

import java.util.UUID;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "delivery")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Delivery {
    @Id
    UUID id;

    @Column(name = "order_id")
    UUID orderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_state")
    DeliveryState deliveryState;
}
