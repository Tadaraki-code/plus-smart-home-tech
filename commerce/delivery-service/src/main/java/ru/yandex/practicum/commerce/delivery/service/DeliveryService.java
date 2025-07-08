package ru.yandex.practicum.commerce.delivery.service;

import ru.yandex.practicum.commerce.interactionapi.delivery.CalculateDeliveryDto;
import ru.yandex.practicum.commerce.interactionapi.delivery.DeliveryDto;

import java.math.BigDecimal;
import java.util.UUID;

public interface DeliveryService {

    DeliveryDto createDelivery(DeliveryDto deliveryDto);

    void successfulDelivery(UUID orderId);

    void pickedItemToDelivery(UUID orderId);

    void failedDelivery(UUID orderId);

    BigDecimal calculateDeliveryCost(CalculateDeliveryDto calculateDeliveryDto);
}
