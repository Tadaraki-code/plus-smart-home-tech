package ru.yandex.practicum.commerce.warehouse.service;

import ru.yandex.practicum.commerce.interactionapi.cart.ShoppingCartDto;
import ru.yandex.practicum.commerce.interactionapi.warehouse.*;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface WarehouseService {

    void addNewProduct(NewProductInWarehouseRequest request);

    void productInShipped(ShippedToDeliveryRequest shippedRequest);

    void returnProductInWarehouse(Map<UUID, Integer> refoundProducts, UUID orderId);

    BookedProductsDto checkProductQuantity(String username, ShoppingCartDto cart);

    BookedProductsDto assemblyProductForDelivery(AssemblyProductsForOrderRequest orderRequest);

    void addProductQuantity(AddProductToWarehouseRequest request);

    AddressDto getWarehouseAddress();

    void cancelReservation(String username, Set<UUID> productIds);
}
