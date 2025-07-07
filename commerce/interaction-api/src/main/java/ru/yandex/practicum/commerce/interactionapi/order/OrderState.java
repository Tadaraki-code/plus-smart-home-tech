package ru.yandex.practicum.commerce.interactionapi.order;

public enum OrderState {
    NEW,
    ON_PAYMENT,
    ON_DELIVERY,
    DONE,
    DELIVERED,
    ASSEMBLED,
    PAID,
    COMPLETED,
    DELIVERY_FAILED,
    DELIVERY_PROBLEMS,
    ASSEMBLY_FAILED,
    PAYMENT_PROBLEMS,
    PAYMENT_FAILED,
    PRODUCT_RETURNED,
    CANCELED
}
