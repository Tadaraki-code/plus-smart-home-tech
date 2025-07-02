package ru.yandex.practicum.commerce.interactionapi.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.commerce.interactionapi.configuration.ShoppingStoreFeignConfig;
import ru.yandex.practicum.commerce.interactionapi.shop.CustomPageResponse;
import ru.yandex.practicum.commerce.interactionapi.shop.ProductDto;
import ru.yandex.practicum.commerce.interactionapi.shop.ProductCategory;
import ru.yandex.practicum.commerce.interactionapi.shop.QuantityState;

import java.util.UUID;

@FeignClient(name = "shopping-store", configuration = ShoppingStoreFeignConfig.class)
public interface ShoppingStoreClient {

    @GetMapping("/api/v1/shopping-store")
    CustomPageResponse<ProductDto> getProducts(
            @RequestParam("category") ProductCategory category,
            @RequestParam("page") int page,
            @RequestParam("size") int size,
            @RequestParam(value = "sort", required = false) String sort);

    @PutMapping("/api/v1/shopping-store")
    ProductDto createNewProduct(@RequestBody ProductDto productDto);

    @PostMapping("/api/v1/shopping-store")
    ProductDto updateProduct(@RequestBody ProductDto productDto);

    @PostMapping("/api/v1/shopping-store/removeProductFromStore")
    Boolean deleteProduct(@RequestBody UUID productId);

    @PostMapping("/api/v1/shopping-store/quantityState")
    Boolean updateQuantityState(
            @RequestParam("productId") UUID productId,
            @RequestParam("quantityState") QuantityState quantityState);

    @GetMapping("/api/v1/shopping-store/{productId}")
    ProductDto getProduct(@PathVariable("productId") UUID productId);
}
