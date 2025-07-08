package ru.yandex.practicum.commerce.interactionapi.delivery;

import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.yandex.practicum.commerce.interactionapi.order.OrderDto;
import ru.yandex.practicum.commerce.interactionapi.warehouse.AddressDto;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CalculateDeliveryDto {
    OrderDto order;
    AddressDto fromAddress;
    AddressDto toAddress;
}
