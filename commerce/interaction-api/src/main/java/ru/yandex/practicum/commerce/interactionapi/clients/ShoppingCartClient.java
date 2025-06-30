package ru.yandex.practicum.commerce.interactionapi.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.commerce.interactionapi.cart.ChangeProductQuantityRequest;
import ru.yandex.practicum.commerce.interactionapi.cart.ShoppingCartDto;
import ru.yandex.practicum.commerce.interactionapi.configuration.ShoppingCartFeignConfig;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@FeignClient(name = "shopping-cart", configuration = ShoppingCartFeignConfig.class)
public interface ShoppingCartClient {

    @GetMapping("/api/v1/shopping-cart")
    ShoppingCartDto getShoppingCart(@RequestParam("username") String username);

    @PutMapping("/api/v1/shopping-cart")
    ShoppingCartDto putItemInCart(
            @RequestParam("username") String username,
            @RequestBody Map<UUID, Integer> items);

    @DeleteMapping("/api/v1/shopping-cart")
    void deleteCart(@RequestParam("username") String username);

    @PostMapping("/api/v1/shopping-cart/remove")
    ShoppingCartDto removeItemFromCart(
            @RequestParam("username") String username,
            @RequestBody List<UUID> items);

    @PostMapping("/api/v1/shopping-cart/change-quantity")
    ShoppingCartDto changeItemQuantity(
            @RequestParam("username") String username,
            @RequestBody ChangeProductQuantityRequest quantityRequest);
}
