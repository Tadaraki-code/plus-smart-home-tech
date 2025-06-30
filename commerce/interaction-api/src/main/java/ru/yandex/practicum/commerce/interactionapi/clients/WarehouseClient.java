package ru.yandex.practicum.commerce.interactionapi.clients;


import org.springframework.cloud.openfeign.FeignClient;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.commerce.interactionapi.cart.ShoppingCartDto;
import ru.yandex.practicum.commerce.interactionapi.configuration.WarehouseFeignConfig;
import ru.yandex.practicum.commerce.interactionapi.warehouse.AddProductToWarehouseRequest;
import ru.yandex.practicum.commerce.interactionapi.warehouse.AddressDto;
import ru.yandex.practicum.commerce.interactionapi.warehouse.BookedProductsDto;
import ru.yandex.practicum.commerce.interactionapi.warehouse.NewProductInWarehouseRequest;

import java.util.UUID;

@FeignClient(name = "warehouse", configuration = WarehouseFeignConfig.class)
public interface WarehouseClient {

    @PutMapping("/api/v1/warehouse")
    void addNewProduct(@RequestBody NewProductInWarehouseRequest request);

    @PostMapping("/api/v1/warehouse/check")
    BookedProductsDto checkProductQuantity(@RequestBody ShoppingCartDto cart);

    @PostMapping("/api/v1/warehouse/add")
    void addProductToWarehouse(@RequestBody AddProductToWarehouseRequest request);

    @GetMapping("/api/v1/warehouse/address")
    AddressDto getWarehouseAddress();

    @DeleteMapping("/api/v1/warehouse/reservation")
    void cancelReservation(@RequestParam UUID shoppingCartId);
}
