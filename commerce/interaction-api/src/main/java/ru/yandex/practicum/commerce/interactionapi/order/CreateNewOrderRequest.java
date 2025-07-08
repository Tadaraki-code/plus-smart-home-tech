package ru.yandex.practicum.commerce.interactionapi.order;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.yandex.practicum.commerce.interactionapi.cart.ShoppingCartDto;
import ru.yandex.practicum.commerce.interactionapi.warehouse.AddressDto;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateNewOrderRequest {
    @NotNull
    String username;
    @NotNull
    ShoppingCartDto shoppingCartDto;
    @NotNull
    AddressDto addressDto;
}
