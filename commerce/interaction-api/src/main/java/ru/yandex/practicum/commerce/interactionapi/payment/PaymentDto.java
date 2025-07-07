package ru.yandex.practicum.commerce.interactionapi.payment;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentDto {
    UUID paymentId;
    PaymentState paymentState;
    BigDecimal totalPayment;
    BigDecimal deliveryTotal;
    BigDecimal feeTotal;
}
