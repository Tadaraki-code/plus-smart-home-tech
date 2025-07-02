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
public class NewProductInWarehouseRequest {

    @NotNull(message = "Необходимо ID товара.")
    UUID productId;
    Boolean fragile;
    @NotNull
    DimensionDto dimension;
    @NotNull(message = "Необходим вес товара.")
    @Min(value = 1, message = "Вес должен быть минимум 1.")
    Double weight;
}
