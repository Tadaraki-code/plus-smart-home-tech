package ru.yandex.practicum.commerce.interactionapi.configuration;

import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.yandex.practicum.commerce.interactionapi.decoders.OrderFeignErrorDecoder;

@Configuration
public class OrderFeignConfig {
    @Bean
    public ErrorDecoder orderErrorDecoder() {
        return new OrderFeignErrorDecoder();
    }
}
