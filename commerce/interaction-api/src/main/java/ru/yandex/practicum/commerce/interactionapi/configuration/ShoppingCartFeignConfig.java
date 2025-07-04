package ru.yandex.practicum.commerce.interactionapi.configuration;

import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.yandex.practicum.commerce.interactionapi.decoders.ShoppingCartFeignErrorDecoder;

@Configuration
public class ShoppingCartFeignConfig {
    @Bean
    public ErrorDecoder shoppingCartErrorDecoder() {
        return new ShoppingCartFeignErrorDecoder();
    }

}
