package ru.yandex.practicum.commerce.interactionapi.decoders;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import feign.Response;
import feign.codec.ErrorDecoder;
import ru.yandex.practicum.commerce.interactionapi.exceptions.*;

import java.io.IOException;

public class WarehouseFeignErrorDecoder implements ErrorDecoder {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ErrorDecoder defaultDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        try {
            ErrorResponse errorResponse = objectMapper.readValue(
                    response.body().asInputStream(), ErrorResponse.class);
            String httpStatus = errorResponse.getHttpStatus();
            String message = errorResponse.getMessage();
            String userMessage = errorResponse.getUserMessage();
            String ex = errorResponse.getEx();

            if (response.status() == 400 && "400 BAD_REQUEST".equals(httpStatus)) {
                switch (ex) {
                    case "SpecifiedProductAlreadyInWarehouseException" -> {
                        return new SpecifiedProductAlreadyInWarehouseException(message, userMessage);
                    }
                    case "NoSpecifiedProductInWarehouseException" -> {
                        return new NoSpecifiedProductInWarehouseException(message, userMessage);
                    }
                    case "ProductInShoppingCartLowQuantityInWarehouse" -> {
                        ErrorResponseForWarehouseChek errorResponseChek = (ErrorResponseForWarehouseChek) errorResponse;
                        return new ProductInShoppingCartLowQuantityInWarehouse(message,
                                errorResponseChek.getMissingProducts());
                    }
                }
                return new IllegalArgumentException(message);
            }
        } catch (IOException e) {
            return new FeignException.InternalServerError(
                    "Failed to parse error response: " + e.getMessage(),
                    response.request(), null, null);
        }

        return defaultDecoder.decode(methodKey, response);
    }
}
