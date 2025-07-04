package ru.yandex.practicum.commerce.interactionapi.warehouse;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AddProductToWarehouseRequest {

    UUID productId;
    @NotNull(message = "Необходимо количества товара.")
    @Min(value = 1, message = "Количество должно быть минимум 1.")
    Integer quantity;
}
