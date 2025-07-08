package ru.yandex.practicum.commerce.interactionapi.warehouse;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookedProductsDto {
    BigDecimal deliveryWeight;
    BigDecimal deliveryVolume;
    Boolean fragile;
}
