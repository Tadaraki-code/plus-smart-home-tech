package ru.yandex.practicum.commerce.order.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.yandex.practicum.commerce.interactionapi.order.CreateNewOrderRequest;
import ru.yandex.practicum.commerce.interactionapi.order.OrderDto;
import ru.yandex.practicum.commerce.interactionapi.order.ProductReturnRequest;

import java.util.UUID;

public interface OrderService {

    Page<OrderDto> getUserOrders(String username, Pageable pageable);

    OrderDto createNewOrder(CreateNewOrderRequest orderRequest);

    OrderDto returnOrder(ProductReturnRequest returnRequest);

    OrderDto orderPayment(UUID orderId);

    void orderPaymentSuccess(UUID orderId);

    OrderDto orderPaymentFailed(UUID orderId);

    OrderDto orderDelivery(UUID orderId);

    void orderDeliverySuccess(UUID orderId);

    OrderDto orderDeliveryFailed(UUID orderId);

    OrderDto orderCompleted(UUID orderId);

    OrderDto calculateTotalOrderCost(UUID orderId);

    OrderDto calculateDeliveryOrderCost(UUID orderId);

    OrderDto orderAssembly(UUID orderId);

    void orderAssemblySuccess(UUID orderId);

    OrderDto orderAssemblyFailed(UUID orderId);
}
