package ru.yandex.practicum.commerce.payment.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.commerce.interactionapi.exceptions.*;

import java.util.Arrays;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotEnoughInfoInOrderToCalculateException.class)
    public ResponseEntity<ErrorResponse> handleNotEnoughInfoInOrderToCalculateException
            (NotEnoughInfoInOrderToCalculateException ex) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setHttpStatus("400 BAD_REQUEST");
        errorResponse.setUserMessage(ex.getUserMessage());
        errorResponse.setMessage(ex.getMessage());
        errorResponse.setEx("NotEnoughInfoInOrderToCalculateException");
        errorResponse.setStackTrace(getStackTrace(ex));
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NoOrderFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoOrderFoundException(NoOrderFoundException ex) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setHttpStatus("404 NOT_FOUND");
        errorResponse.setUserMessage(ex.getUserMessage());
        errorResponse.setEx("NoOrderFoundException");
        errorResponse.setMessage(ex.getMessage());
        errorResponse.setStackTrace(getStackTrace(ex));
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(IllegalStateException ex) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setHttpStatus("400 BAD_REQUEST");
        errorResponse.setUserMessage(ex.getMessage());
        errorResponse.setEx("IllegalStateException");
        errorResponse.setMessage(ex.getMessage());
        errorResponse.setStackTrace(getStackTrace(ex));
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleProductNotFoundException(ProductNotFoundException ex) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setHttpStatus("404 NOT_FOUND");
        errorResponse.setUserMessage(ex.getMessage());
        errorResponse.setEx("ProductNotFoundException");
        errorResponse.setMessage(ex.getMessage());
        errorResponse.setStackTrace(getStackTrace(ex));
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }


    private StackTraceElementDto[] getStackTrace(Throwable ex) {
        return Arrays.stream(ex.getStackTrace())
                .map(ste -> new StackTraceElementDto(
                        ste.getClassLoaderName(),
                        ste.getModuleName(),
                        ste.getModuleVersion(),
                        ste.getMethodName(),
                        ste.getFileName(),
                        ste.getLineNumber(),
                        ste.getClassName(),
                        ste.isNativeMethod()))
                .toArray(StackTraceElementDto[]::new);
    }
}
