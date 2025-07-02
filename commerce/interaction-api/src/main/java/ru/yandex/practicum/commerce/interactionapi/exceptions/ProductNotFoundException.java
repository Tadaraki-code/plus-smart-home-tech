package ru.yandex.practicum.commerce.interactionapi.exceptions;

import lombok.Getter;

@Getter
public class ProductNotFoundException extends RuntimeException {
  private final String userMessage;
  private final String httpStatus;

  public ProductNotFoundException(String message, String userMessage) {
    super(message);
    this.userMessage = userMessage;
    this.httpStatus = "404 NOT_FOUND";
  }

}
