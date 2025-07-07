package ru.yandex.practicum.commerce.payment.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.commerce.interactionapi.payment.PaymentDto;
import ru.yandex.practicum.commerce.payment.model.Payment;

@Component
public class PaymentMapper {

    public PaymentDto toDto(Payment payment) {
        return PaymentDto.builder()
                .paymentId(payment.getId())
                .paymentState(payment.getPaymentState())
                .totalPayment(payment.getTotalPayment())
                .deliveryTotal(payment.getDeliveryTotal())
                .feeTotal(payment.getFeeTotal())
                .build();
    }
}
