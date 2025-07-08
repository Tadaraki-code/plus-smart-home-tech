package ru.yandex.practicum.commerce.warehouse.controller;


import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.commerce.interactionapi.cart.ShoppingCartDto;
import ru.yandex.practicum.commerce.interactionapi.warehouse.*;
import ru.yandex.practicum.commerce.warehouse.service.WarehouseService;

import java.util.Map;
import java.util.Set;
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

    @PostMapping("/shipped")
    public void productInShipped(@RequestBody ShippedToDeliveryRequest shippedRequest) {
        log.info("Получен запрос на передачу собраного заказа на склад: {}", shippedRequest);
        warehouseService.productInShipped(shippedRequest);
    }

    @PostMapping("/return")
    public void returnProductInWarehouse(@RequestBody Map<UUID, Integer> refoundProducts,
                                         @RequestParam @NotBlank UUID orderId) {
        log.info("Получен запрос на возврат товаров на склад: {}", refoundProducts);
        warehouseService.returnProductInWarehouse(refoundProducts, orderId);
    }


    @PostMapping("/check")
    public BookedProductsDto checkProductQuantity(@RequestParam @NotBlank String username,
                                                  @RequestBody @Valid ShoppingCartDto cart) {
        log.info("Проверка доступности товаров по корзине: {}", cart);
        return warehouseService.checkProductQuantity(username, cart);
    }

    @PostMapping("/assembly")
    public BookedProductsDto assemblyProductForDelivery(@RequestBody AssemblyProductsForOrderRequest orderRequest) {
        log.info("Получен запрос на сборку заказа для доставки: {}", orderRequest);
        return warehouseService.assemblyProductForDelivery(orderRequest);
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
    public void cancelReservation(@RequestParam @NotBlank String username, @RequestBody Set<UUID> productIds) {
        log.info("Получен запрос на отмену резервации для корзины пользователя с ником: {}", username);
        warehouseService.cancelReservation(username, productIds);
    }
}