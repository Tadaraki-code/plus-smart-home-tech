package ru.yandex.practicum.commerce.order.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.yandex.practicum.commerce.interactionapi.order.OrderState;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "orders")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Order {

    @Id
    UUID id;

    @Column(name = "shopping_cart_id")
    UUID shoppingCartId;

    @Column(name = "username")
    String username;

    @ElementCollection
    @CollectionTable(
            name = "order_items",
            joinColumns = @JoinColumn(name = "order_id")
    )
    @MapKeyColumn(name = "product_id")
    @Column(name = "quantity")
    @Builder.Default
    Map<UUID, Integer> products = new HashMap<>();

    @Column(name = "payment_id")
    UUID paymentId;

    @Column(name = "delivery_id")
    UUID deliveryId;

    @Enumerated(EnumType.STRING)
    @Column(name = "state")
    OrderState state;

    @Column(name = "delivery_weight")
    BigDecimal deliveryWeight;

    @Column(name = "delivery_volume")
    BigDecimal deliveryVolume;

    @Column(name = "fragile")
    Boolean fragile;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @PrimaryKeyJoinColumn
    OrderAddress address;

    @Column(name = "total_price")
    BigDecimal totalPrice;

    @Column(name = "delivery_price")
    BigDecimal deliveryPrice;

    @Column(name = "product_price")
    BigDecimal productPrice;
}
