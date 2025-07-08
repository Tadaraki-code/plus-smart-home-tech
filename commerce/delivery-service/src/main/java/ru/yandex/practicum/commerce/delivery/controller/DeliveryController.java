package ru.yandex.practicum.commerce.delivery.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.commerce.delivery.service.DeliveryService;
import ru.yandex.practicum.commerce.interactionapi.delivery.CalculateDeliveryDto;
import ru.yandex.practicum.commerce.interactionapi.delivery.DeliveryDto;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/delivery")
public class DeliveryController {
    private final DeliveryService deliveryService;

    @PutMapping
    public DeliveryDto planDelivery(@RequestBody DeliveryDto deliveryDto) {
        log.info("Планируем доставку для DTO: {}", deliveryDto);
        DeliveryDto result = deliveryService.createDelivery(deliveryDto);
        log.info("Доставка успешно запланирована: {}", result);
        return result;
    }

    @PostMapping("/successful")
    public void successfulDelivery(@RequestParam UUID orderId) {
        log.info("Отмечаем доставку как успешную для заказа ID: {}", orderId);
        deliveryService.successfulDelivery(orderId);
        log.info("Доставка отмечена как успешная для заказа ID: {}", orderId);
    }

    @PostMapping("/picked")
    public void pickedItemToDelivery(@RequestParam UUID orderId) {
        log.info("Отмечаем товар как забранный для доставки для заказа ID: {}", orderId);
        deliveryService.pickedItemToDelivery(orderId);
        log.info("Товар отмечен как забранный для доставки для заказа ID: {}", orderId);
    }

    @PostMapping("/failed")
    public void failedDelivery(@RequestParam UUID orderId) {
        log.info("Отмечаем доставку как неудавшуюся для заказа ID: {}", orderId);
        deliveryService.failedDelivery(orderId);
        log.info("Доставка отмечена как неудавшаяся для заказа ID: {}", orderId);
    }

    @PostMapping("/cost")
    public BigDecimal calculateDeliveryCost(@RequestBody CalculateDeliveryDto calculateDeliveryDto) {
        log.info("Рассчитываем стоимость доставки для заказа DTO: {}", calculateDeliveryDto);
        BigDecimal cost = deliveryService.calculateDeliveryCost(calculateDeliveryDto);
        log.info("Стоимость доставки рассчитана: {}", cost);
        return cost;
    }
}
