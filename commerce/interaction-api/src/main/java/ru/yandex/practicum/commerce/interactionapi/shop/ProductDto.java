package ru.yandex.practicum.commerce.interactionapi.shop;

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
public class ProductDto {
    UUID productId;

    @NotNull(message = "Необходимо имя продукта.")
    String productName;

    @NotNull(message = "Необходимо описание продукта.")
    String description;

    String imageSrc;

    @NotNull(message = "Необходим индикатор количества продукта.")
    QuantityState quantityState;

    @NotNull(message = "Необходимо состояние открытости продукта.")
    ProductState productState;

    ProductCategory productCategory;

    @NotNull(message = "Необходима цена.")
    @Min(value = 1, message = "Цена должна быть минимум 1.")
    Double price;
}
