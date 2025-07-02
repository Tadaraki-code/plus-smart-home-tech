package ru.yandex.practicum.commerce.warehouse.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.commerce.interactionapi.cart.ShoppingCartDto;
import ru.yandex.practicum.commerce.interactionapi.warehouse.AddProductToWarehouseRequest;
import ru.yandex.practicum.commerce.interactionapi.warehouse.AddressDto;
import ru.yandex.practicum.commerce.interactionapi.warehouse.BookedProductsDto;
import ru.yandex.practicum.commerce.interactionapi.warehouse.NewProductInWarehouseRequest;
import ru.yandex.practicum.commerce.warehouse.service.WarehouseService;

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/warehouse")
public class WarehouseController {

    private final WarehouseService warehouseService;

    @PutMapping
    public void newProductInWarehouse(@RequestBody @Valid NewProductInWarehouseRequest request) {
        log.info("Получен запрос на добавление нового товара в склад: {}", request);
        warehouseService.addNewProduct(request);
    }

    @PostMapping("/check")
    public BookedProductsDto checkProductQuantityEnoughForShoppingCart(@RequestBody @Valid ShoppingCartDto cart) {
        log.info("Проверка доступности товаров по корзине: {}", cart);
        return warehouseService.checkProductQuantity(cart);
    }

    @PostMapping("/add")
    public void addProductToWarehouse(@RequestBody @Valid AddProductToWarehouseRequest request) {
        log.info("Получен запрос на пополнение товара на складе: {}", request);
        warehouseService.addProductQuantity(request);
    }

    @GetMapping("/address")
    public AddressDto getWarehouseAddress() {
        log.info("Получен запрос на получение адреса склада");
        return warehouseService.getWarehouseAddress();
    }

    @DeleteMapping("/reservation")
    public void cancelReservation(@RequestParam UUID shoppingCartId) {
        log.info("Получен запрос на отмену резервации для корзины: {}", shoppingCartId);
        warehouseService.cancelReservation(shoppingCartId);
    }
}