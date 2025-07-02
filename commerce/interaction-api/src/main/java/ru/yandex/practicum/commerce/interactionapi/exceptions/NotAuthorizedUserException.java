package ru.yandex.practicum.commerce.interactionapi.exceptions;

import lombok.Getter;

@Getter
public class NotAuthorizedUserException extends RuntimeException {
    private final String userMessage;
    private final String httpStatus;

    public NotAuthorizedUserException(String message, String userMessage) {
        super(message);
        this.userMessage = userMessage;
        this.httpStatus = "401 UNAUTHORIZED";
    }

}
