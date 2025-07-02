package ru.yandex.practicum.commerce.shoppingstore.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.yandex.practicum.commerce.interactionapi.shop.ProductCategory;
import ru.yandex.practicum.commerce.interactionapi.shop.ProductDto;
import ru.yandex.practicum.commerce.interactionapi.shop.QuantityState;

import java.util.UUID;

public interface ShoppingStoreService {

    Page<ProductDto> getProducts(ProductCategory category, Pageable pageable);

    ProductDto createNewProduct(ProductDto productDto);

    ProductDto updateProduct(ProductDto productDto);

    Boolean deleteProduct(UUID productId);

    Boolean updateQuantityState(UUID productId, QuantityState state);

    ProductDto getProduct(UUID productId);
}
