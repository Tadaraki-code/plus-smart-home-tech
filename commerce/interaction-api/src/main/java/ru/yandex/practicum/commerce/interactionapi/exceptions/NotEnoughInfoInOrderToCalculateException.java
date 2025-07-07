package ru.yandex.practicum.commerce.interactionapi.exceptions;

import lombok.Getter;

@Getter
public class NotEnoughInfoInOrderToCalculateException extends RuntimeException {
    private final String userMessage;
    private final String httpStatus;

    public NotEnoughInfoInOrderToCalculateException(String message, String userMessage) {
        super(message);
        this.userMessage = userMessage;
        this.httpStatus = "400 BAD_REQUEST";
    }
}
