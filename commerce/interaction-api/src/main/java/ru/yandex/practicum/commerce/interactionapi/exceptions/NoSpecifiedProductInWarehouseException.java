package ru.yandex.practicum.commerce.interactionapi.exceptions;

import lombok.Getter;

@Getter
public class NoSpecifiedProductInWarehouseException extends RuntimeException {
    private final String userMessage;
    private final String httpStatus;

    public NoSpecifiedProductInWarehouseException(String message, String userMessage) {
        super(message);
        this.userMessage = userMessage;
        this.httpStatus = "400 BAD_REQUEST";
    }

}
