package ru.yandex.practicum.commerce.interactionapi.configuration;

import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.yandex.practicum.commerce.interactionapi.decoders.DeliveryFeignErrorDecoder;

@Configuration
public class DeliveryFeignConfig {
    @Bean
    public ErrorDecoder deliveryErrorDecoder() {
        return new DeliveryFeignErrorDecoder();
    }
}
