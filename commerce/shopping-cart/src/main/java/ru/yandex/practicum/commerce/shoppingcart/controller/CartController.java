package ru.yandex.practicum.commerce.shoppingcart.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.commerce.interactionapi.cart.ChangeProductQuantityRequest;
import ru.yandex.practicum.commerce.interactionapi.cart.ShoppingCartDto;
import ru.yandex.practicum.commerce.shoppingcart.service.CartService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
public class CartController {
    private static final String API_PREFIX = "/api/v1/shopping-cart";
    private static final String API_PREFIX_REMOVE = "/remove";
    private static final String API_PREFIX_CHANGE_QUANTITY = "/change-quantity";

    private final CartService cartService;

    @GetMapping(API_PREFIX)
    public ShoppingCartDto getShoppingCart(@RequestParam @NotBlank String username) {
        log.info("Запрос на получение корзины для пользователя: {}", username);
        return cartService.getShoppingCart(username);
    }

    @PutMapping(API_PREFIX)
    public ShoppingCartDto putItemInCart(@RequestParam @NotBlank String username,
                                         @RequestBody Map<UUID, Integer> items) {
        log.info("Добавление товаров в корзину пользователя {}: {}", username, items);
        return cartService.putItemInCart(username, items);
    }

    @DeleteMapping(API_PREFIX)
    public void deleteCart(@RequestParam @NotBlank String username) {
        log.info("Удаление корзины для пользователя: {}", username);
        cartService.deleteCart(username);
    }

    @PostMapping(API_PREFIX + API_PREFIX_REMOVE)
    public ShoppingCartDto removeItemFromCart(@RequestParam @NotBlank String username,
                                              @RequestBody List<UUID> items) {
        log.info("Удаление товаров {} из корзины пользователя {}", items, username);
        return cartService.removeItemFromCart(username, items);
    }

    @PostMapping(API_PREFIX + API_PREFIX_CHANGE_QUANTITY)
    public ShoppingCartDto changeItemQuantity(@RequestParam @NotBlank String username,
                                              @RequestBody @Valid ChangeProductQuantityRequest quantityRequest) {
        log.info("Изменение количества товара {} на {} в корзине пользователя {}",
                quantityRequest.getProductId(), quantityRequest.getNewQuantity(), username);
        return cartService.changeItemQuantity(username, quantityRequest);
    }
}