package ru.yandex.practicum.commerce.shoppingcart.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.commerce.interactionapi.cart.ShoppingCartDto;
import ru.yandex.practicum.commerce.shoppingcart.model.Cart;

@Component
public class CartMapper {

    public ShoppingCartDto toDto(Cart cart) {
        return ShoppingCartDto.builder()
                .shoppingCartId(cart.getId())
                .products(cart.getProducts())
                .build();
    }
}
