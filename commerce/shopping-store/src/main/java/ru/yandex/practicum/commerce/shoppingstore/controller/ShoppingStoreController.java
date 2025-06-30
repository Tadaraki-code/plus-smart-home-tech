package ru.yandex.practicum.commerce.shoppingstore.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.commerce.interactionapi.shop.CustomPageResponse;
import ru.yandex.practicum.commerce.interactionapi.shop.ProductCategory;
import ru.yandex.practicum.commerce.interactionapi.shop.ProductDto;
import ru.yandex.practicum.commerce.interactionapi.shop.QuantityState;
import ru.yandex.practicum.commerce.shoppingstore.service.ShoppingStoreService;

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ShoppingStoreController {
    private static final String API_PREFIX = "/api/v1/shopping-store";
    private static final String API_PREFIX_REMOVE_PRODUCT = "/removeProductFromStore";
    private static final String API_PREFIX_QUANTITY_STATE = "/quantityState";
    private static final String PRODUCT_ID = "/{productId}";

    private final ShoppingStoreService shoppingStoreService;

    @GetMapping(API_PREFIX)
    public CustomPageResponse<ProductDto> getProducts(@RequestParam ProductCategory category, Pageable pageable) {
        log.info("Запрошен список продуктов. Категория: {}, пагинация: {}", category, pageable);
        Page<ProductDto> result = shoppingStoreService.getProducts(category, pageable);
        log.info("Получено {} продуктов", result.getTotalElements());
        return new CustomPageResponse<>(result);
    }

    @PutMapping(API_PREFIX)
    public ProductDto createNewProduct(@RequestBody @Valid ProductDto productDto) {
        log.info("Создание нового продукта: {}", productDto);
        ProductDto result = shoppingStoreService.createNewProduct(productDto);
        log.info("Создан продукт: {}", result);
        return result;
    }

    @PostMapping(API_PREFIX)
    public ProductDto updateProduct(@RequestBody @Valid ProductDto productDto) {
        log.info("Обновление продукта: {}", productDto);
        ProductDto result = shoppingStoreService.updateProduct(productDto);
        log.info("Обновлён продукт: {}", result);
        return result;
    }

    @PostMapping(API_PREFIX + API_PREFIX_REMOVE_PRODUCT)
    public boolean deleteProduct(@RequestBody UUID productId) {
        log.info("Удаление продукта с ID: {}", productId);
        boolean result = shoppingStoreService.deleteProduct(productId);
        log.info("Результат удаления: {}", result);
        return result;
    }

    @PostMapping(API_PREFIX + API_PREFIX_QUANTITY_STATE)
    public boolean updateQuantityState(@RequestParam UUID productId, QuantityState quantityState) {
        log.info("Обновление количества продукта c ID {}", productId);
        boolean result = shoppingStoreService.updateQuantityState(productId, quantityState);
        log.info("Результат обновления количества: {}", result);
        return result;
    }

    @GetMapping(API_PREFIX + PRODUCT_ID)
    public ProductDto getProduct(@PathVariable UUID productId) {
        log.info("Запрос продукта по ID: {}", productId);
        ProductDto result = shoppingStoreService.getProduct(productId);
        log.info("Получен продукт: {}", result);
        return result;
    }
}

