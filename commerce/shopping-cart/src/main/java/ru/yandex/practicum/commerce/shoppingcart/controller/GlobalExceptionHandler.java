package ru.yandex.practicum.commerce.shoppingcart.controller;

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

    @ExceptionHandler(NotAuthorizedUserException.class)
    public ResponseEntity<ErrorResponse> handleNotAuthorizedUserException(NotAuthorizedUserException ex) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setHttpStatus("401 UNAUTHORIZED");
        errorResponse.setUserMessage(ex.getUserMessage());
        errorResponse.setMessage(ex.getMessage());
        errorResponse.setStackTrace(getStackTrace(ex));
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(ProductInShoppingCartLowQuantityInWarehouse.class)
    public ResponseEntity<ErrorResponseForWarehouseChek> handleProductInShoppingCartLowQuantityInWarehouse
            (ProductInShoppingCartLowQuantityInWarehouse ex) {
        ErrorResponseForWarehouseChek errorResponse = new ErrorResponseForWarehouseChek();
        errorResponse.setHttpStatus("400 BAD_REQUEST");
        errorResponse.setUserMessage(ex.getUserMessage());
        errorResponse.setMessage(ex.getMessage());
        errorResponse.setEx("ProductInShoppingCartLowQuantityInWarehouse");
        errorResponse.setStackTrace(getStackTrace(ex));
        errorResponse.setMissingProducts(ex.getMissingProducts());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NoProductsInShoppingCartException.class)
    public ResponseEntity<ErrorResponse> handleNoProductsInShoppingCartException(NoProductsInShoppingCartException ex) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setHttpStatus("400 BAD_REQUEST");
        errorResponse.setUserMessage(ex.getUserMessage());
        errorResponse.setEx("NoProductsInShoppingCartException");
        errorResponse.setMessage(ex.getMessage());
        errorResponse.setStackTrace(getStackTrace(ex));
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
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