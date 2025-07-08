package ru.yandex.practicum.commerce.interactionapi.decoders;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.commerce.interactionapi.exceptions.ErrorResponse;
import ru.yandex.practicum.commerce.interactionapi.exceptions.NoOrderFoundException;
import ru.yandex.practicum.commerce.interactionapi.exceptions.NotEnoughInfoInOrderToCalculateException;
import ru.yandex.practicum.commerce.interactionapi.exceptions.ProductNotFoundException;

import java.io.IOException;

@Slf4j
@Component
public class PaymentFeignErrorDecoder implements ErrorDecoder {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ErrorDecoder defaultDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        try {
            if (response.body() == null) {
                return new FeignException.InternalServerError("Пустое тело ответа от Feign",
                        response.request(), null, null);
            }

            ErrorResponse errorResponse = objectMapper.readValue(
                    response.body().asInputStream(), ErrorResponse.class);

            String httpStatus = errorResponse.getHttpStatus();
            String message = errorResponse.getMessage();
            String userMessage = errorResponse.getUserMessage();
            String ex = errorResponse.getEx();

            switch (response.status()) {
                case 404:
                    if ("404 NOT_FOUND".equals(httpStatus)) {
                        if ("NoOrderFoundException".equals(ex)) {
                            return new NoOrderFoundException(message, userMessage);
                        } else if ("ProductNotFoundException".equals(ex)) {
                            return new ProductNotFoundException(message, userMessage);
                        }
                    }
                    break;

                case 400:
                    if ("400 BAD_REQUEST".equals(httpStatus)) {
                        if ("NotEnoughInfoInOrderToCalculateException".equals(ex)) {
                            return new NotEnoughInfoInOrderToCalculateException(message, userMessage);
                        } else if ("IllegalStateException".equals(ex)) {
                            return new IllegalStateException(message);
                        }
                        return new IllegalArgumentException(message);
                    }
                    break;

                default:
                    log.error("Unhandled Feign error: status={}, ex={}, message={}", httpStatus, ex, message);
                    return new IllegalStateException("Необработанная ошибка: " + message);
            }

        } catch (IOException e) {
            log.error("Ошибка парсинга тела ответа от Feign", e);
            return new FeignException.InternalServerError(
                    "Failed to parse error response: " + e.getMessage(),
                    response.request(), null, null);
        }

        return defaultDecoder.decode(methodKey, response);
    }
}
