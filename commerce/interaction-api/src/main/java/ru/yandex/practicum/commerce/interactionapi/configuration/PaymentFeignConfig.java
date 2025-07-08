package ru.yandex.practicum.commerce.interactionapi.configuration;

import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.yandex.practicum.commerce.interactionapi.decoders.PaymentFeignErrorDecoder;

@Configuration
public class PaymentFeignConfig {
    @Bean
    public ErrorDecoder paymentErrorDecoder() {
        return new PaymentFeignErrorDecoder();
    }
}
