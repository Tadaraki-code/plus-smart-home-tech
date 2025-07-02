package ru.yandex.practicum.commerce.warehouse.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.commerce.interactionapi.cart.ShoppingCartDto;
import ru.yandex.practicum.commerce.interactionapi.exceptions.NoSpecifiedProductInWarehouseException;
import ru.yandex.practicum.commerce.interactionapi.exceptions.ProductInShoppingCartLowQuantityInWarehouse;
import ru.yandex.practicum.commerce.interactionapi.exceptions.SpecifiedProductAlreadyInWarehouseException;
import ru.yandex.practicum.commerce.interactionapi.warehouse.AddProductToWarehouseRequest;
import ru.yandex.practicum.commerce.interactionapi.warehouse.AddressDto;
import ru.yandex.practicum.commerce.interactionapi.warehouse.BookedProductsDto;
import ru.yandex.practicum.commerce.interactionapi.warehouse.NewProductInWarehouseRequest;
import ru.yandex.practicum.commerce.warehouse.model.Reservation;
import ru.yandex.practicum.commerce.warehouse.model.WarehouseItem;
import ru.yandex.practicum.commerce.warehouse.storage.ReservationRepository;
import ru.yandex.practicum.commerce.warehouse.storage.WarehouseItemRepository;

import java.security.SecureRandom;
import java.util.*;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Slf4j
public class WarehouseServiceImpl implements WarehouseService {

    private final WarehouseItemRepository itemRepository;
    private final ReservationRepository reservationRepository;

    private static final String[] ADDRESSES = new String[]{"ADDRESS_1", "ADDRESS_2"};
    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    public void addNewProduct(NewProductInWarehouseRequest request) {
        log.info("Попытка добавить новый товар на склад: {}", request);
        if (itemRepository.existsById(request.getProductId())) {
            log.info("Товар с ID {} уже существует на складе", request.getProductId());
            throw new SpecifiedProductAlreadyInWarehouseException(
                    "Товар с ID " + request.getProductId() + " уже зарегистрирован на складе",
                    "Товар с ID " + request.getProductId() + " уже зарегистрирован на складе");
        }
        WarehouseItem item = WarehouseItem.builder()
                .id(request.getProductId())
                .fragile(request.getFragile())
                .width(request.getDimension().getWidth())
                .height(request.getDimension().getHeight())
                .depth(request.getDimension().getDepth())
                .weight(request.getWeight())
                .quantity(0)
                .build();
        itemRepository.save(item);
        log.info("Товар с ID {} успешно добавлен на склад", request.getProductId());
    }

    @Override
    @Transactional
    public BookedProductsDto checkProductQuantity(ShoppingCartDto cart) {
        log.info("Проверка наличия товаров по корзине: {}", cart.getShoppingCartId());

        Map<UUID, WarehouseItem> products = itemRepository.findAllById(cart.getProducts().keySet())
                .stream()
                .collect(HashMap::new,
                        (map, item) -> map.put(item.getId(), item),
                        HashMap::putAll);

        double totalWeight = 0;
        double totalVolume = 0;
        boolean hasFragile = false;
        List<UUID> missingProducts = new ArrayList<>();

        for (Map.Entry<UUID, Integer> entry : cart.getProducts().entrySet()) {
            UUID productId = entry.getKey();
            long requestedQuantity = entry.getValue();
            WarehouseItem product = products.get(productId);

            if (product == null) {
                log.info("Товар с ID {} не найден", productId);
                throw new NoSpecifiedProductInWarehouseException("Товар " + productId + " не найден",
                        "Товар " + productId + " не найден");
            }

            if (product.getQuantity() < requestedQuantity) {
                log.info("Недостаточно товара на складе: {} (есть {}, нужно {})",
                        productId, product.getQuantity(), requestedQuantity);
                missingProducts.add(productId);
            } else {
                totalWeight += product.getWeight() * requestedQuantity;
                totalVolume += (product.getWidth() * product.getHeight() * product.getDepth()) * requestedQuantity;
                hasFragile = hasFragile || product.isFragile();
            }
        }

        if (!missingProducts.isEmpty()) {
            log.info("Недостаток товаров на складе для корзины {}: {}", cart.getShoppingCartId(), missingProducts);
            throw new ProductInShoppingCartLowQuantityInWarehouse("Недостаточно товаров на складе",
                    missingProducts);
        }

        for (Map.Entry<UUID, Integer> entry : cart.getProducts().entrySet()) {
            UUID productId = entry.getKey();
            long requestedQuantity = entry.getValue();
            WarehouseItem product = products.get(productId);

            product.setQuantity(product.getQuantity() - requestedQuantity);
            product.setReservedQuantity(product.getReservedQuantity() + requestedQuantity);
            itemRepository.save(product);

            Reservation reservation = new Reservation();
            reservation.setShoppingCartId(cart.getShoppingCartId());
            reservation.setProductId(productId);
            reservation.setReservedQuantity(requestedQuantity);
            reservationRepository.save(reservation);

            log.info("Зарезервировано {} ед. товара {} для корзины {}", requestedQuantity, productId,
                    cart.getShoppingCartId());
        }

        BookedProductsDto result = new BookedProductsDto();
        result.setDeliveryWeight(totalWeight);
        result.setDeliveryVolume(totalVolume);
        result.setFragile(hasFragile);
        log.info("Резервация для корзины {} завершена успешно. Вес: {}, Объем: {}, Хрупкий: {}",
                cart.getShoppingCartId(), totalWeight, totalVolume, hasFragile);
        return result;
    }

    @Override
    @Transactional
    public void addProductQuantity(AddProductToWarehouseRequest request) {
        log.info("Попытка пополнить товар {} на складе на {} единиц", request.getProductId(), request.getQuantity());
        WarehouseItem item = itemRepository.findById(request.getProductId())
                .orElseThrow(() -> {
                    log.error("Товар с ID {} не найден на складе", request.getProductId());
                    return new NoSpecifiedProductInWarehouseException(
                            "Товар с ID " + request.getProductId() + " не найден на складе",
                            "Товар с ID " + request.getProductId() + " не найден на складе");
                });
        item.setQuantity(item.getQuantity() + request.getQuantity());
        itemRepository.save(item);
        log.info("Товар {} успешно пополнен на {} единиц. Новое количество: {}",
                request.getProductId(), request.getQuantity(), item.getQuantity());
    }

    @Override
    public AddressDto getWarehouseAddress() {
        String address = ADDRESSES[RANDOM.nextInt(ADDRESSES.length)];
        log.info("Получен запрос на адрес склада. Отдаем: {}", address);
        return AddressDto.builder()
                .country(address)
                .city(address)
                .street(address)
                .house(address)
                .flat(address)
                .build();
    }

    @Override
    @Transactional
    public void cancelReservation(UUID shoppingCartId) {
        log.info("Отмена резервации для корзины {}", shoppingCartId);
        List<Reservation> reservations = reservationRepository.findByShoppingCartId(shoppingCartId);

        Map<UUID, WarehouseItem> products = itemRepository.findAllById(
                reservations.stream().map(Reservation::getProductId).toList()
        ).stream().collect(HashMap::new,
                (map, item) -> map.put(item.getId(), item), HashMap::putAll);

        for (Reservation reservation : reservations) {
            WarehouseItem item = products.get(reservation.getProductId());
            if (item == null) {
                log.info("Товар с ID {} не найден при отмене резервации", reservation.getProductId());
                throw new NoSpecifiedProductInWarehouseException(
                        "Товар " + reservation.getProductId() + " не найден",
                        "Товар " + reservation.getProductId() + " не найден");
            }

            item.setQuantity(item.getQuantity() + reservation.getReservedQuantity());
            item.setReservedQuantity(item.getReservedQuantity() - reservation.getReservedQuantity());
            itemRepository.save(item);

            log.info("Резервация {} ед. товара {} отменена, корзина {}",
                    reservation.getReservedQuantity(), reservation.getProductId(), shoppingCartId);

            reservationRepository.delete(reservation);
        }
        log.info("Все резервации для корзины {} успешно отменены", shoppingCartId);
    }
}


