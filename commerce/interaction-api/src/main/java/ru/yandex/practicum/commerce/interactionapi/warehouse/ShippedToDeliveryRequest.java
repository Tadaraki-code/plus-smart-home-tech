package ru.yandex.practicum.commerce.interactionapi.warehouse;


import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ShippedToDeliveryRequest {
    UUID orderId;
    UUID deliveryId;
}
