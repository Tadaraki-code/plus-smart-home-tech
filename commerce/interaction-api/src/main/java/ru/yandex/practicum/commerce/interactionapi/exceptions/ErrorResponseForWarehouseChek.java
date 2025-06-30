package ru.yandex.practicum.commerce.interactionapi.exceptions;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class ErrorResponseForWarehouseChek extends ErrorResponse {
    private List<UUID> missingProducts;
}
