package ru.yandex.practicum.commerce.interactionapi.exceptions;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ErrorResponse {
    private String httpStatus;
    private String userMessage;
    private String message;
    private String ex;
    private StackTraceElementDto[] stackTrace;
    private Object cause;
    private Object[] suppressed;
    private String localizedMessage;
}

