package ru.yandex.practicum.commerce.payment.service;

import ru.yandex.practicum.commerce.interactionapi.order.OrderDto;
import ru.yandex.practicum.commerce.interactionapi.payment.PaymentDto;

import java.math.BigDecimal;
import java.util.UUID;

public interface PaymentService {

    PaymentDto payment(OrderDto order);

    BigDecimal calculateTotalCost(OrderDto orderDto);

    void refund(UUID paymentId);

    BigDecimal calculateProductCost(OrderDto orderDto);

    void paymentFailed(UUID paymentId);

    void paymentSuccess(UUID paymentId);
}
