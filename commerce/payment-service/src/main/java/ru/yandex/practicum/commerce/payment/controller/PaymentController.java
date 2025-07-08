package ru.yandex.practicum.commerce.payment.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.commerce.interactionapi.order.OrderDto;
import ru.yandex.practicum.commerce.interactionapi.payment.PaymentDto;
import ru.yandex.practicum.commerce.payment.service.PaymentService;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/payment")
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping
    public PaymentDto payment(@RequestBody OrderDto orderDto) {
        log.info("Получен запрос на оплату заказа id: {}", orderDto.getOrderId());
        PaymentDto paymentDto = paymentService.payment(orderDto);
        log.info("Оплата заказа id: {} завершена со статусом: {}", orderDto.getOrderId(), paymentDto.getPaymentState());
        return paymentDto;
    }

    @PostMapping("/totalCost")
    public BigDecimal calculateTotalCost(@RequestBody OrderDto orderDto) {
        log.info("Запрос на расчет общей стоимости заказа id: {}", orderDto.getOrderId());
        BigDecimal totalCost = paymentService.calculateTotalCost(orderDto);
        log.info("Общая стоимость заказа id: {} рассчитана: {}", orderDto.getOrderId(), totalCost);
        return totalCost;
    }

    @PostMapping("/refund")
    public void refund(@RequestParam UUID paymentId) {
        log.info("Запрос на возврат платежа id: {}", paymentId);
        paymentService.refund(paymentId);
        log.info("Возврат платежа id: {} выполнен", paymentId);
    }

    @PostMapping("/productCost")
    public BigDecimal calculateProductCost(@RequestBody OrderDto orderDto) {
        log.info("Запрос на расчет стоимости товаров для заказа id: {}", orderDto.getOrderId());
        BigDecimal productCost = paymentService.calculateProductCost(orderDto);
        log.info("Стоимость товаров для заказа id: {} рассчитана: {}", orderDto.getOrderId(), productCost);
        return productCost;
    }

    @PostMapping("/failed")
    public void paymentFailed(@RequestParam UUID paymentId) {
        log.info("Обработка статуса 'оплата не прошла' для платежа id: {}", paymentId);
        paymentService.paymentFailed(paymentId);
        log.info("Оплата не прошла для платежа id: {}", paymentId);
    }

    @PostMapping("/success")
    public void paymentSuccess(@RequestParam UUID paymentId) {
        log.info("Обработка успешной оплаты для платежа id: {}", paymentId);
        paymentService.paymentSuccess(paymentId);
        log.info("Оплата успешно завершена для платежа id: {}", paymentId);
    }
}
