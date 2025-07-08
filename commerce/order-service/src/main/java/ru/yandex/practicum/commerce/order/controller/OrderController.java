package ru.yandex.practicum.commerce.order.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.commerce.interactionapi.order.CreateNewOrderRequest;
import ru.yandex.practicum.commerce.interactionapi.order.OrderDto;
import ru.yandex.practicum.commerce.interactionapi.order.ProductReturnRequest;
import ru.yandex.practicum.commerce.order.service.OrderService;

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/order")
public class OrderController {
    private final OrderService orderService;

    @GetMapping
    public Page<OrderDto> getUserOrders(@RequestParam String username, Pageable pageable) {
        log.info("Получаем заказы пользователя: {}", username);
        Page<OrderDto> result = orderService.getUserOrders(username, pageable);
        log.info("Получено {} заказов для пользователя: {}", result.getTotalElements(), username);
        return result;
    }

    @PutMapping
    public OrderDto createNewOrder(@RequestBody @Valid CreateNewOrderRequest orderRequest) {
        log.info("Создаем новый заказ: {}", orderRequest);
        OrderDto result = orderService.createNewOrder(orderRequest);
        log.info("Заказ успешно создан: {}", result);
        return result;
    }

    @PostMapping("/return")
    public OrderDto returnOrder(@RequestBody ProductReturnRequest returnRequest) {
        log.info("Обрабатываем возврат товара: {}", returnRequest);
        OrderDto result = orderService.returnOrder(returnRequest);
        log.info("Возврат товара успешно обработан: {}", result);
        return result;
    }

    @PostMapping("/payment")
    public OrderDto orderPayment(@RequestParam UUID orderId) {
        log.info("Обрабатываем оплату заказа ID: {}", orderId);
        OrderDto result = orderService.orderPayment(orderId);
        log.info("Оплата заказа ID: {} обработана: {}", orderId, result);
        return result;
    }

    @PostMapping("/payment/failed")
    public OrderDto orderPaymentFailed(@RequestParam UUID orderId) {
        log.info("Отмечаем оплату заказа ID: {} как неудавшуюся", orderId);
        OrderDto result = orderService.orderPaymentFailed(orderId);
        log.info("Оплата заказа ID: {} отмечена как неудавшаяся: {}", orderId, result);
        return result;
    }

    @PostMapping("/payment/success")
    public void orderPaymentSuccess(@RequestParam UUID orderId) {
        log.info("Отмечаем оплату заказа ID: {} как успешную", orderId);
        orderService.orderPaymentSuccess(orderId);
        log.info("Оплата заказа ID: {} успешно отмечена", orderId);
    }

    @PostMapping("/delivery")
    public OrderDto orderDelivery(@RequestParam UUID orderId) {
        log.info("Обрабатываем доставку заказа ID: {}", orderId);
        OrderDto result = orderService.orderDelivery(orderId);
        log.info("Доставка заказа ID: {} обработана: {}", orderId, result);
        return result;
    }

    @PostMapping("/delivery/failed")
    public OrderDto orderDeliveryFailed(@RequestParam UUID orderId) {
        log.info("Отмечаем доставку заказа ID: {} как неудавшуюся", orderId);
        OrderDto result = orderService.orderDeliveryFailed(orderId);
        log.info("Доставка заказа ID: {} отмечена как неудавшаяся: {}", orderId, result);
        return result;
    }

    @PostMapping("/delivery/success")
    public void orderDeliverySuccess(@RequestParam UUID orderId) {
        log.info("Отмечаем доставку заказа ID: {} как успешную", orderId);
        orderService.orderDeliverySuccess(orderId);
        log.info("Доставка заказа ID: {} успешно отмечена", orderId);
    }

    @PostMapping("/completed")
    public OrderDto orderCompleted(@RequestParam UUID orderId) {
        log.info("Отмечаем заказ ID: {} как завершенный", orderId);
        OrderDto result = orderService.orderCompleted(orderId);
        log.info("Заказ ID: {} завершен: {}", orderId, result);
        return result;
    }

    @PostMapping("/calculate/total")
    public OrderDto calculateTotalOrderCost(@RequestParam UUID orderId) {
        log.info("Рассчитываем общую стоимость заказа ID: {}", orderId);
        OrderDto result = orderService.calculateTotalOrderCost(orderId);
        log.info("Общая стоимость заказа ID: {} рассчитана: {}", orderId, result);
        return result;
    }

    @PostMapping("/calculate/delivery")
    public OrderDto calculateDeliveryOrderCost(@RequestParam UUID orderId) {
        log.info("Рассчитываем стоимость доставки заказа ID: {}", orderId);
        OrderDto result = orderService.calculateDeliveryOrderCost(orderId);
        log.info("Стоимость доставки заказа ID: {} рассчитана: {}", orderId, result);
        return result;
    }

    @PostMapping("/assembly")
    public OrderDto orderAssembly(@RequestParam UUID orderId) {
        log.info("Обрабатываем сборку заказа ID: {}", orderId);
        OrderDto result = orderService.orderAssembly(orderId);
        log.info("Сборка заказа ID: {} обработана: {}", orderId, result);
        return result;
    }

    @PostMapping("/assembly/success")
    public void orderAssemblySuccess(@RequestParam UUID orderId) {
        log.info("Отмечаем сборку заказа ID: {} как успешную", orderId);
        orderService.orderAssemblySuccess(orderId);
        log.info("Сборка заказа ID: {} успешно отмечена", orderId);
    }

    @PostMapping("/assembly/failed")
    public OrderDto orderAssemblyFailed(@RequestParam UUID orderId) {
        log.info("Отмечаем сборку заказа ID: {} как неудавшуюся", orderId);
        OrderDto result = orderService.orderAssemblyFailed(orderId);
        log.info("Сборка заказа ID: {} отмечена как неудавшаяся: {}", orderId, result);
        return result;
    }
}
