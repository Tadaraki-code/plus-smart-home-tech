package ru.yandex.practicum.commerce.payment.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.yandex.practicum.commerce.interactionapi.payment.PaymentState;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "payments")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Payment {
    @Id
    UUID id;
    @Column(name = "order_id")
    UUID orderId;
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_state")
    PaymentState paymentState;
    @Column(name = "total_price")
    BigDecimal totalPayment;
    @Column(name = "delivery_price")
    BigDecimal deliveryTotal;
    @Column(name = "fee_total")
    BigDecimal feeTotal;
}
