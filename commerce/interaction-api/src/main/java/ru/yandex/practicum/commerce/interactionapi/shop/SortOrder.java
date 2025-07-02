package ru.yandex.practicum.commerce.interactionapi.shop;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SortOrder {
    private final String property;
    private final String direction;
}
