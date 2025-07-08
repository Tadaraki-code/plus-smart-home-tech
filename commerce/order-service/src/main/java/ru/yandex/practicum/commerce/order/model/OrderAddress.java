package ru.yandex.practicum.commerce.order.model;


import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Entity
@Table(name = "order_address")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderAddress {
    @Id
    @Column(name = "order_id")
    UUID orderId;

    @OneToOne
    @MapsId
    @ToString.Exclude
    Order order;

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
