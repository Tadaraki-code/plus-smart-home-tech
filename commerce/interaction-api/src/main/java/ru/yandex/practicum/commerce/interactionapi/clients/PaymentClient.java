package ru.yandex.practicum.commerce.interactionapi.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.practicum.commerce.interactionapi.decoders.PaymentFeignErrorDecoder;
import ru.yandex.practicum.commerce.interactionapi.order.OrderDto;
import ru.yandex.practicum.commerce.interactionapi.payment.PaymentDto;

import java.math.BigDecimal;
import java.util.UUID;

@FeignClient(name = "payment-service", configuration = PaymentFeignErrorDecoder.class)
public interface PaymentClient {

    @PostMapping("/api/v1/payment")
    PaymentDto payment(@RequestBody OrderDto order);

    @PostMapping("/api/v1/payment/totalCost")
    BigDecimal calculateTotalCost(@RequestBody OrderDto orderDto);

    @PostMapping("/api/v1/payment/refund")
    void refund(@RequestParam UUID paymentId);

    @PostMapping("/api/v1/payment/productCost")
    BigDecimal calculateProductCost(@RequestBody OrderDto orderDto);

    @PostMapping("/api/v1/payment/failed")
    void paymentFailed(@RequestParam UUID paymentId);

    @PostMapping("/api/v1/payment/success")
    void paymentSuccess(@RequestParam UUID paymentId);
}
