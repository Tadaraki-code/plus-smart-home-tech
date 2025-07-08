package ru.yandex.practicum.commerce.interactionapi.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.commerce.interactionapi.decoders.OrderFeignErrorDecoder;
import ru.yandex.practicum.commerce.interactionapi.order.CreateNewOrderRequest;
import ru.yandex.practicum.commerce.interactionapi.order.OrderDto;
import ru.yandex.practicum.commerce.interactionapi.order.ProductReturnRequest;

import java.util.UUID;

@FeignClient(name = "order-service", configuration = OrderFeignErrorDecoder.class)
public interface OrderClient {

    @GetMapping("/api/v1/order")
    OrderDto getUserOrders(@RequestParam String username);

    @PutMapping("/api/v1/order")
    OrderDto createNewOrder(@RequestBody CreateNewOrderRequest orderRequest);

    @PostMapping("/api/v1/order/return")
    OrderDto returnOrder(@RequestBody ProductReturnRequest returnRequest);

    @PostMapping("/api/v1/order/payment")
    OrderDto orderPayment(@RequestParam UUID orderId);

    @PostMapping("/api/v1/order/payment/success")
    void orderPaymentSuccess(@RequestParam UUID orderId);

    @PostMapping("/api/v1/order/payment/failed")
    OrderDto orderPaymentFailed(@RequestParam UUID orderId);

    @PostMapping("/api/v1/order/delivery")
    OrderDto orderDelivery(@RequestParam UUID orderId);

    @PostMapping("/api/v1/order/delivery/success")
    void orderDeliverySuccess(@RequestParam UUID orderId);

    @PostMapping("/api/v1/order/delivery/failed")
    OrderDto orderDeliveryFailed(@RequestParam UUID orderId);

    @PostMapping("/api/v1/order/completed")
    OrderDto orderCompleted(@RequestParam UUID orderId);

    @PostMapping("/api/v1/order/calculate/total")
    OrderDto calculateTotalOrderCost(@RequestParam UUID orderId);

    @PostMapping("/api/v1/order/calculate/delivery")
    OrderDto calculateDeliveryOrderCost(@RequestParam UUID orderId);

    @PostMapping("/api/v1/order/assembly")
    OrderDto orderAssembly(@RequestParam UUID orderId);

    @PostMapping("/api/v1/order/assembly/success")
    void orderAssemblySuccess(@RequestParam UUID orderId);

    @PostMapping("/api/v1/order/assembly/failed")
    OrderDto orderAssemblyFailed(@RequestParam UUID orderId);
}
