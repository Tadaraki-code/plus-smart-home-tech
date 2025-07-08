package ru.yandex.practicum.commerce.interactionapi.order;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderDto {

    UUID orderId;
    String username;
    UUID shoppingCartId;
    Map<UUID, Integer> products;
    UUID paymentId;
    UUID deliveryId;
    OrderState state;
    BigDecimal deliveryWeight;
    BigDecimal deliveryVolume;
    Boolean fragile;
    BigDecimal totalPrice;
    BigDecimal deliveryPrice;
    BigDecimal productPrice;
}
