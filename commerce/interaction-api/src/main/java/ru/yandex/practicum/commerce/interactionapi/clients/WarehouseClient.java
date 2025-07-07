package ru.yandex.practicum.commerce.interactionapi.clients;


import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.cloud.openfeign.FeignClient;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.commerce.interactionapi.cart.ShoppingCartDto;
import ru.yandex.practicum.commerce.interactionapi.configuration.WarehouseFeignConfig;
import ru.yandex.practicum.commerce.interactionapi.warehouse.*;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

@FeignClient(name = "warehouse", configuration = WarehouseFeignConfig.class)
public interface WarehouseClient {

    @PutMapping("/api/v1/warehouse")
    void addNewProduct(@RequestBody NewProductInWarehouseRequest request);

    @PostMapping("/api/v1/warehouse/shipped")
    void productInShipped(@RequestBody ShippedToDeliveryRequest shippedRequest);

    @PostMapping("/api/v1/warehouse/return")
    void returnProductInWarehouse(@RequestBody Map<UUID, Integer> refoundProducts,
                                  @RequestParam @NotBlank UUID orderId);

    @PostMapping("/api/v1/warehouse/check")
    BookedProductsDto checkProductQuantity(@RequestParam @NotBlank String username,
                                           @RequestBody @Valid ShoppingCartDto cart);

    @PostMapping("/api/v1/warehouse/assembly")
    BookedProductsDto assemblyProductForDelivery(@RequestBody AssemblyProductsForOrderRequest orderRequest);

    @PostMapping("/api/v1/warehouse/add")
    void addProductToWarehouse(@RequestBody AddProductToWarehouseRequest request);

    @GetMapping("/api/v1/warehouse/address")
    AddressDto getWarehouseAddress();

    @DeleteMapping("/api/v1/warehouse/reservation")
    void cancelReservation(@RequestParam @NotBlank String username, @RequestBody Set<UUID> productIds);
}
