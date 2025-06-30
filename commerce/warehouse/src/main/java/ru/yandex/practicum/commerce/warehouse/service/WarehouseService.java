package ru.yandex.practicum.commerce.warehouse.service;

import ru.yandex.practicum.commerce.interactionapi.cart.ShoppingCartDto;
import ru.yandex.practicum.commerce.interactionapi.warehouse.AddProductToWarehouseRequest;
import ru.yandex.practicum.commerce.interactionapi.warehouse.AddressDto;
import ru.yandex.practicum.commerce.interactionapi.warehouse.BookedProductsDto;
import ru.yandex.practicum.commerce.interactionapi.warehouse.NewProductInWarehouseRequest;

import java.util.UUID;

public interface WarehouseService {

    void addNewProduct(NewProductInWarehouseRequest request);

    BookedProductsDto checkProductQuantity(ShoppingCartDto cart);

    void addProductQuantity(AddProductToWarehouseRequest request);

    AddressDto getWarehouseAddress();

    void cancelReservation(UUID shoppingCartId);
}
