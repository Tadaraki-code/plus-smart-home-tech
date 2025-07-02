package ru.yandex.practicum.commerce.interactionapi.warehouse;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DimensionDto {

    @NotNull(message = "Необходима ширина товара.")
    @Min(value = 1, message = "Ширина должна быть минимум 1.")
    Double width;
    @NotNull(message = "Необходима высота товара.")
    @Min(value = 1, message = "Высота должна быть минимум 1.")
    Double height;
    @NotNull(message = "Необходима глубина товара.")
    @Min(value = 1, message = "Глубина должна быть минимум 1.")
    Double depth;
}
