package ru.yandex.practicum.commerce.warehouse.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.commerce.interactionapi.cart.ShoppingCartDto;
import ru.yandex.practicum.commerce.interactionapi.exceptions.NoSpecifiedProductInWarehouseException;
import ru.yandex.practicum.commerce.interactionapi.exceptions.ProductInShoppingCartLowQuantityInWarehouse;
import ru.yandex.practicum.commerce.interactionapi.exceptions.SpecifiedProductAlreadyInWarehouseException;
import ru.yandex.practicum.commerce.interactionapi.warehouse.*;
import ru.yandex.practicum.commerce.warehouse.model.OrderBooking;
import ru.yandex.practicum.commerce.warehouse.model.Reservation;
import ru.yandex.practicum.commerce.warehouse.model.WarehouseItem;
import ru.yandex.practicum.commerce.warehouse.storage.OrderBookingRepository;
import ru.yandex.practicum.commerce.warehouse.storage.ReservationRepository;
import ru.yandex.practicum.commerce.warehouse.storage.WarehouseItemRepository;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Slf4j
public class WarehouseServiceImpl implements WarehouseService {

    private final WarehouseItemRepository itemRepository;
    private final ReservationRepository reservationRepository;
    private final OrderBookingRepository orderBookingRepository;

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
    public void productInShipped(ShippedToDeliveryRequest shippedRequest) {
        log.info("Передача заказа {} в доставку, deliveryId: {}", shippedRequest.getOrderId(),
                shippedRequest.getDeliveryId());

        List<OrderBooking> bookings = orderBookingRepository.findByOrderId(shippedRequest.getOrderId());
        if (bookings.isEmpty()) {
            log.error("Бронирования для заказа {} не найдены", shippedRequest.getOrderId());
            throw new NoSpecifiedProductInWarehouseException("Бронирования для заказа " +
                    shippedRequest.getOrderId() + " не найдены",
                    "Бронирования для заказа " + shippedRequest.getOrderId() + " не найдены");
        }

        for (OrderBooking booking : bookings) {
            booking.setDeliveryId(shippedRequest.getDeliveryId());
            orderBookingRepository.save(booking);
            log.info("Для бронирования товара {} заказа {} установлен deliveryId: {}",
                    booking.getProductId(), shippedRequest.getOrderId(), shippedRequest.getDeliveryId());
        }

        log.info("Заказ {} успешно передан в доставку", shippedRequest.getOrderId());
    }

    @Override
    @Transactional
    public void returnProductInWarehouse(Map<UUID, Integer> refoundProducts, UUID orderId) {
        log.info("Возврат товаров на склад: {}", refoundProducts);

        Map<UUID, WarehouseItem> products = itemRepository.findAllById(refoundProducts.keySet())
                .stream()
                .collect(HashMap::new, (map,
                                        item) -> map.put(item.getId(), item), HashMap::putAll);

        for (Map.Entry<UUID, Integer> entry : refoundProducts.entrySet()) {
            UUID productId = entry.getKey();
            int returnQuantity = entry.getValue();
            WarehouseItem product = products.get(productId);

            if (product == null) {
                log.error("Товар с ID {} не найден на складе", productId);
                throw new NoSpecifiedProductInWarehouseException("Товар " + productId + " не найден",
                        "Товар " + productId + " не найден");
            }

            product.setQuantity(product.getQuantity() + returnQuantity);
            itemRepository.save(product);
            log.info("Товар {} возвращен на склад в количестве {}. Новое количество: {}",
                    productId, returnQuantity, product.getQuantity());
        }
        List<OrderBooking> bookings = orderBookingRepository.findByOrderId(orderId);
        if (!bookings.isEmpty()) {
            orderBookingRepository.deleteAll(bookings);
        }


        log.info("Возврат товаров завершен успешно");
    }

    @Override
    @Transactional
    public BookedProductsDto checkProductQuantity(String username, ShoppingCartDto cart) {
        log.info("Проверка корзины: {}", cart.getShoppingCartId());

        Map<UUID, WarehouseItem> products = getProducts(cart.getProducts().keySet());
        List<UUID> missingProducts = validateProductQuantity(cart.getProducts(), products);

        if (!missingProducts.isEmpty()) {
            throw new ProductInShoppingCartLowQuantityInWarehouse("Недостаточно товаров", missingProducts);
        }

        BookedProductsDto dto = reserveProducts(username, cart.getShoppingCartId(), cart.getProducts(), products);

        log.info("Корзина {} зарезервирована", cart.getShoppingCartId());

        return dto;
    }

    @Override
    @Transactional
    public BookedProductsDto assemblyProductForDelivery(AssemblyProductsForOrderRequest orderRequest) {
        log.info("Сборка товаров для заказа {}", orderRequest.getOrderId());

        Map<UUID, WarehouseItem> products = getProducts(orderRequest.getProducts().keySet());
        List<UUID> missingProducts = validateProductQuantity(orderRequest.getProducts(), products);

        if (!missingProducts.isEmpty()) {
            throw new ProductInShoppingCartLowQuantityInWarehouse("Недостаточно товаров для формировния заказа",
                    missingProducts);
        }
        cancelReservation(orderRequest.getUsername(), products.keySet());
        BookedProductsDto dto = bookProductsForOrder(orderRequest.getOrderId(), orderRequest.getProducts(), products);

        log.info("Сборка заказа {} завершена", orderRequest.getOrderId());

        return dto;
    }


    @Override
    @Transactional
    public void addProductQuantity(AddProductToWarehouseRequest request) {
        log.info("Попытка пополнить товар {} на складе на {} единиц", request.getProductId(), request.getQuantity());
        WarehouseItem item = itemRepository.findById(request.getProductId())
                .orElseThrow(() -> {
                    log.error("Ошибка при добавлении товара, товар с ID {} не найден на складе", request.getProductId());
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
    public void cancelReservation(String username, Set<UUID> productIds) {
        log.info("Отмена резервации для корзины пользователя с ником  {}", username);
        List<Reservation> reservations = reservationRepository.findByUsernameAndProductIdIn(username, productIds);

        Map<UUID, WarehouseItem> products = itemRepository.findAllById(
                        reservations.stream().map(Reservation::getProductId).toList())
                .stream()
                .collect(HashMap::new,
                        (map, item) -> map.put(item.getId(), item),
                        HashMap::putAll);

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

            log.info("Резервация {} ед. товара {} отменена, ник пользователя {}",
                    reservation.getReservedQuantity(), reservation.getProductId(), username);

            reservationRepository.delete(reservation);
        }
        log.info("Резервации для корзины пользователя {} успешно отменены", username);
    }


    private Map<UUID, WarehouseItem> getProducts(Set<UUID> ids) {
        return itemRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(WarehouseItem::getId, Function.identity()));
    }

    private List<UUID> validateProductQuantity(Map<UUID, Integer> requested, Map<UUID, WarehouseItem> available) {
        List<UUID> missing = new ArrayList<>();

        requested.forEach((id, qty) -> {
            WarehouseItem item = available.get(id);
            if (item == null || item.getQuantity() < qty) missing.add(id);
        });

        return missing;
    }

    private BookedProductsDto reserveProducts(String username, UUID cartId,
                                              Map<UUID, Integer> request,
                                              Map<UUID, WarehouseItem> products) {
        BigDecimal weight = BigDecimal.ZERO;
        BigDecimal volume = BigDecimal.ZERO;
        boolean fragile = false;

        List<Reservation> reservationsToSave = new ArrayList<>();
        List<WarehouseItem> itemsToSave = new ArrayList<>();

        Map<UUID, Reservation> existingReservations = reservationRepository
                .findAllByShoppingCartIdAndUsernameAndProductIdIn(cartId, username, request.keySet())
                .stream()
                .collect(Collectors.toMap(Reservation::getProductId, Function.identity()));

        for (UUID productId : request.keySet()) {
            int qty = request.get(productId);
            WarehouseItem item = products.get(productId);

            weight = weight.add(item.getWeight().multiply(BigDecimal.valueOf(qty)));
            volume = volume.add(item.getWidth().multiply(item.getHeight()).multiply(item.getDepth())
                    .multiply(BigDecimal.valueOf(qty)));
            fragile |= item.isFragile();

            Reservation reservation = existingReservations.get(productId);
            if (reservation == null) {
                if (item.getQuantity() < qty) {
                    throw new IllegalArgumentException("Недостаточно товара на складе " + productId);
                }
                item.setQuantity(item.getQuantity() - qty);
                item.setReservedQuantity(item.getReservedQuantity() + qty);

                reservation = Reservation.builder()
                        .shoppingCartId(cartId)
                        .username(username)
                        .productId(productId)
                        .reservedQuantity(qty)
                        .build();
                reservationsToSave.add(reservation);
            } else if (reservation.getReservedQuantity() != qty) {
                long diff = qty - reservation.getReservedQuantity();
                if (diff > 0 && item.getQuantity() < diff) {
                    throw new IllegalArgumentException("Недостаточно товарана складе " + productId);
                }
                item.setQuantity(item.getQuantity() - diff);
                item.setReservedQuantity(item.getReservedQuantity() + diff);
                reservation.setReservedQuantity(qty);
                reservationsToSave.add(reservation);
            }
            itemsToSave.add(item);
        }

        itemRepository.saveAll(itemsToSave);
        reservationRepository.saveAll(reservationsToSave);

        return new BookedProductsDto(weight, volume, fragile);
    }

    private BookedProductsDto bookProductsForOrder(UUID orderId, Map<UUID, Integer> request,
                                                   Map<UUID, WarehouseItem> products) {
        BigDecimal weight = BigDecimal.ZERO;
        BigDecimal volume = BigDecimal.ZERO;
        boolean fragile = false;

        List<OrderBooking> bookings = new ArrayList<>();

        for (UUID productId : request.keySet()) {
            int qty = request.get(productId);
            WarehouseItem item = products.get(productId);

            item.setQuantity(item.getQuantity() - qty);

            weight = weight.add(item.getWeight().multiply(BigDecimal.valueOf(qty)));
            volume = volume.add(item.getWidth()
                    .multiply(item.getHeight())
                    .multiply(item.getDepth())
                    .multiply(BigDecimal.valueOf(qty)));

            fragile |= item.isFragile();

            bookings.add(OrderBooking.builder()
                    .orderId(orderId)
                    .productId(item.getId())
                    .bookedQuantity(qty)
                    .build());
        }

        orderBookingRepository.saveAll(bookings);
        itemRepository.saveAll(products.values());

        return new BookedProductsDto(weight, volume, fragile);
    }
}



