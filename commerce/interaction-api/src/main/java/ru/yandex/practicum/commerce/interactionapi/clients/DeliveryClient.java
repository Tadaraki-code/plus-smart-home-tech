package ru.yandex.practicum.commerce.interactionapi.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.practicum.commerce.interactionapi.decoders.DeliveryFeignErrorDecoder;
import ru.yandex.practicum.commerce.interactionapi.delivery.CalculateDeliveryDto;
import ru.yandex.practicum.commerce.interactionapi.delivery.DeliveryDto;

import java.math.BigDecimal;
import java.util.UUID;

@FeignClient(name = "delivery-service", configuration = DeliveryFeignErrorDecoder.class)
public interface DeliveryClient {

    @PutMapping("/api/v1/delivery")
    DeliveryDto createDelivery(@RequestBody DeliveryDto deliveryDto);

    @PostMapping("/api/v1/delivery/successful")
    void successfulDelivery(@RequestParam UUID orderId);

    @PostMapping("/api/v1/delivery/picked")
    void pickedItemToDelivery(@RequestParam UUID orderId);

    @PostMapping("/api/v1/delivery/failed")
    void failedDelivery(@RequestParam UUID orderId);

    @PostMapping("/api/v1/delivery/cost")
    BigDecimal calculateDeliveryCost(@RequestBody CalculateDeliveryDto calculateDeliveryDto);
}
