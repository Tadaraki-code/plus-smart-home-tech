package ru.yandex.practicum.commerce.delivery.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Entity
@Table(name = "delivery_address")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DeliveryAddress {
    @Id
    @Column(name = "delivery_id")
    UUID deliveryId;

    @Column(name = "country", nullable = false)
    String country;

    @Column(name = "city", nullable = false)
    String city;

    @Column(name = "street", nullable = false)
    String street;

    @Column(name = "house", nullable = false)
    String house;

    @Column(name = "flat", nullable = false)
    String flat;
}
