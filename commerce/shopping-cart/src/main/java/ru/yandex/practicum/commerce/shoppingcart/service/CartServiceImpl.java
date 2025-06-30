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
    public ShoppingCartDto getShoppingCart(String userName) {
        Cart cart = getOrCreateCart(userName);
        return cartMapper.toDto(cart);
    }

    @Override
    public ShoppingCartDto putItemInCart(String userName, Map<UUID, Integer> items) {
        Cart cart = getOrCreateCart(userName);
        cart.getProducts().putAll(items);

        try {
            warehouseClient.checkProductQuantity(cartMapper.toDto(cart));
        } catch (ProductInShoppingCartLowQuantityInWarehouse e) {
            log.info("Недостаточно товаров на складе для корзины {}", cart.getId());
            throw e;
        }

        cartRepository.save(cart);
        log.info("Добавлены товары в корзину пользователя {}: {}", userName, items.keySet());
        return cartMapper.toDto(cart);
    }

    @Override
    public void deleteCart(String userName) {
        cartRepository.findByUserName(userName)
                .ifPresent(cart -> {
                    cartRepository.deleteById(cart.getId());
                    log.info("Корзина пользователя {} удалена", userName);
                });
    }

    @Override
    public ShoppingCartDto removeItemFromCart(String userName, List<UUID> items) {
        Cart cart = cartRepository.findByUserName(userName)
                .orElseThrow(() -> new NotFoundException("Корзина пользователя с именем " + userName + " не найдена"));

        for (UUID itemId : items) {
            if (!cart.getProducts().containsKey(itemId)) {
                throw new NoProductsInShoppingCartException(
                        "Товара с ID " + itemId + " нет в корзине",
                        "Товара с ID " + itemId + " нет в корзине");
            }
            cart.getProducts().remove(itemId);
            log.info("Товар с ID {} удалён из корзины пользователя {}", itemId, userName);
        }

        cartRepository.save(cart);
        return cartMapper.toDto(cart);
    }

    @Override
    public ShoppingCartDto changeItemQuantity(String userName, ChangeProductQuantityRequest quantityRequest) {
        Cart cart = cartRepository.findByUserName(userName)
                .orElseThrow(() -> new NotFoundException("Корзина пользователя с именем " + userName + " не найдена"));

        UUID productId = quantityRequest.getProductId();
        if (!cart.getProducts().containsKey(productId)) {
            throw new NoProductsInShoppingCartException(
                    "Товара с ID " + productId + " нет в корзине",
                    "Товара с ID " + productId + " нет в корзине");
        }

        cart.getProducts().put(productId, quantityRequest.getNewQuantity());
        cartRepository.save(cart);
        log.info("Изменено количество товара {} в корзине пользователя {} на {}",
                productId, userName, quantityRequest.getNewQuantity());
        return cartMapper.toDto(cart);
    }

    private Cart getOrCreateCart(String userName) {
        return cartRepository.findByUserName(userName)
                .orElseGet(() -> {
                    Cart newCart = new Cart(UUID.randomUUID(), userName, new HashMap<>());
                    cartRepository.save(newCart);
                    log.info("Создана новая корзина для пользователя {}", userName);
                    return newCart;
                });
    }
}
