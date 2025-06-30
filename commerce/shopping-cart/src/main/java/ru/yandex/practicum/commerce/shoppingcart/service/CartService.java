package ru.yandex.practicum.commerce.shoppingcart.service;

import ru.yandex.practicum.commerce.interactionapi.cart.ChangeProductQuantityRequest;
import ru.yandex.practicum.commerce.interactionapi.cart.ShoppingCartDto;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface CartService {

    ShoppingCartDto getShoppingCart(String userName);

    ShoppingCartDto putItemInCart(String userName, Map<UUID, Integer> items);

    void deleteCart(String userName);

    ShoppingCartDto removeItemFromCart(String userName, List<UUID> items);

    ShoppingCartDto changeItemQuantity(String userName, ChangeProductQuantityRequest quantityRequest);
}
