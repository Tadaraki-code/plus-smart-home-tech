package ru.yandex.practicum.commerce.shoppingcart.service;

import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.commerce.interactionapi.cart.ChangeProductQuantityRequest;
import ru.yandex.practicum.commerce.interactionapi.cart.ShoppingCartDto;
import ru.yandex.practicum.commerce.interactionapi.clients.WarehouseClient;
import ru.yandex.practicum.commerce.interactionapi.exceptions.NoProductsInShoppingCartException;
import ru.yandex.practicum.commerce.interactionapi.exceptions.ProductInShoppingCartLowQuantityInWarehouse;
import ru.yandex.practicum.commerce.shoppingcart.mapper.CartMapper;
import ru.yandex.practicum.commerce.shoppingcart.model.Cart;
import ru.yandex.practicum.commerce.shoppingcart.storage.CartRepository;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {
    private final CartRepository cartRepository;
    private final CartMapper cartMapper;
    private final WarehouseClient warehouseClient;

    @Override
    public ShoppingCartDto getShoppingCart(String username) {
        Cart cart = getOrCreateCart(username);
        return cartMapper.toDto(cart);
    }

    @Override
    public ShoppingCartDto putItemInCart(String username, Map<UUID, Integer> items) {
        Cart cart = getOrCreateCart(username);
        cart.getProducts().putAll(items);

        try {
            warehouseClient.checkProductQuantity(username, cartMapper.toDto(cart));
        } catch (ProductInShoppingCartLowQuantityInWarehouse e) {
            log.info("Недостаточно товаров на складе для корзины {}", cart.getId());
            throw e;
        }

        cartRepository.save(cart);
        log.info("Добавлены товары в корзину пользователя {}: {}", username, items.keySet());
        return cartMapper.toDto(cart);
    }

    @Override
    public void deleteCart(String username) {
        findCart(username).ifPresent(cart -> {
            cartRepository.deleteById(cart.getId());
            log.info("Корзина пользователя {} удалена", username);
        });
    }

    @Override
    public ShoppingCartDto removeItemFromCart(String username, List<UUID> items) {
        Cart cart = getCartOrThrow(username);

        for (UUID itemId : items) {
            validateProductInCart(cart, itemId);
            cart.getProducts().remove(itemId);
            log.info("Товар с ID {} удалён из корзины пользователя {}", itemId, username);
        }

        cartRepository.save(cart);
        return cartMapper.toDto(cart);
    }

    @Override
    public ShoppingCartDto changeItemQuantity(String username, ChangeProductQuantityRequest quantityRequest) {
        Cart cart = getCartOrThrow(username);

        UUID productId = quantityRequest.getProductId();
        validateProductInCart(cart, productId);

        cart.getProducts().put(productId, quantityRequest.getNewQuantity());
        cartRepository.save(cart);
        log.info("Изменено количество товара {} в корзине пользователя {} на {}",
                productId, username, quantityRequest.getNewQuantity());
        return cartMapper.toDto(cart);
    }

    private void validateProductInCart(Cart cart, UUID productId) {
        if (!cart.getProducts().containsKey(productId)) {
            throw new NoProductsInShoppingCartException(
                    "Товара с ID " + productId + " нет в корзине",
                    "Товара с ID " + productId + " нет в корзине"
            );
        }
    }

    private Optional<Cart> findCart(String username) {
        return cartRepository.findByUserName(username);
    }

    private Cart getCartOrThrow(String username) {
        return findCart(username)
                .orElseThrow(() -> new NotFoundException("Корзина пользователя с именем " + username + " не найдена"));
    }

    private Cart getOrCreateCart(String username) {
        return findCart(username)
                .orElseGet(() -> {
                    Cart newCart = new Cart(UUID.randomUUID(), username, new HashMap<>());
                    cartRepository.save(newCart);
                    log.info("Создана новая корзина для пользователя {}", username);
                    return newCart;
                });
    }
}
