package ru.yandex.practicum.commerce.interactionapi.warehouse;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Map;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AssemblyProductsForOrderRequest {

    String username;
    Map<UUID, Integer> products;
    UUID orderId;
}
