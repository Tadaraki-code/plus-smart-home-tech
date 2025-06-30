package ru.yandex.practicum.commerce.interactionapi.configuration;

import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.yandex.practicum.commerce.interactionapi.decoders.ShoppingStoreFeignErrorDecoder;

@Configuration
public class ShoppingStoreFeignConfig {

    @Bean
    public ErrorDecoder shoppingStoreErrorDecoder() {
        return new ShoppingStoreFeignErrorDecoder();
    }
}
