package ru.yandex.practicum.commerce.delivery.service;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.commerce.delivery.mapper.DeliveryMapper;
import ru.yandex.practicum.commerce.delivery.model.Delivery;
import ru.yandex.practicum.commerce.delivery.model.DeliveryAddress;
import ru.yandex.practicum.commerce.delivery.model.WarehouseAddress;
import ru.yandex.practicum.commerce.delivery.storage.DeliveryAddressRepository;
import ru.yandex.practicum.commerce.delivery.storage.DeliveryRepository;
import ru.yandex.practicum.commerce.delivery.storage.WarehouseAddressRepository;
import ru.yandex.practicum.commerce.interactionapi.clients.OrderClient;
import ru.yandex.practicum.commerce.interactionapi.clients.WarehouseClient;
import ru.yandex.practicum.commerce.interactionapi.delivery.CalculateDeliveryDto;
import ru.yandex.practicum.commerce.interactionapi.delivery.DeliveryDto;
import ru.yandex.practicum.commerce.interactionapi.delivery.DeliveryState;
import ru.yandex.practicum.commerce.interactionapi.exceptions.NoDeliveryFoundException;
import ru.yandex.practicum.commerce.interactionapi.exceptions.NoOrderFoundException;
import ru.yandex.practicum.commerce.interactionapi.order.OrderDto;
import ru.yandex.practicum.commerce.interactionapi.warehouse.AddressDto;
import ru.yandex.practicum.commerce.interactionapi.warehouse.ShippedToDeliveryRequest;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryServiceImpl implements DeliveryService {
    private final DeliveryRepository deliveryRepository;
    private final DeliveryAddressRepository deliveryAddressRepository;
    private final WarehouseAddressRepository warehouseAddressRepository;
    private final DeliveryMapper deliveryMapper;
    private final OrderClient orderClient;
    private final WarehouseClient warehouseClient;

    @Override
    @Transactional
    public DeliveryDto createDelivery(DeliveryDto deliveryDto) {
        log.info("Создание доставки для заказа {}", deliveryDto.getOrderId());

        if (deliveryDto.getOrderId() == null) {
            log.info("Попытка создать доставку без указания orderId");
            throw new IllegalStateException("В запросе на создание доставки не указан id заказа");
        }

        if (deliveryDto.getToAddress() == null || deliveryDto.getFromAddress() == null) {
            log.info("Попытка создать доставку без указания адресов");
            throw new IllegalStateException("В запросе на создание доставки не указан адрес доставки/склада заказа");
        }

        UUID deliveryId = UUID.randomUUID();
        log.info("Сгенерирован deliveryId {}", deliveryId);

        Delivery delivery = deliveryMapper.fromDto(deliveryDto, deliveryId);
        DeliveryAddress deliveryAddress = deliveryMapper.toDeliveryAddress(deliveryDto.getToAddress(), deliveryId);
        WarehouseAddress warehouseAddress = deliveryMapper.toWarehouseAddress(deliveryDto.getFromAddress(), deliveryId);
        delivery.setDeliveryState(DeliveryState.CREATED);

        deliveryRepository.save(delivery);
        deliveryAddressRepository.save(deliveryAddress);
        warehouseAddressRepository.save(warehouseAddress);

        log.info("Доставка с id {} успешно создана", deliveryId);

        return deliveryMapper.toDto(delivery, deliveryAddress, warehouseAddress);
    }

    @Override
    @Transactional
    public void successfulDelivery(UUID orderId) {
        log.info("Попытка завершить доставку для заказа {}", orderId);

        Delivery delivery = findDeliveryByOrderId(orderId);

        if (delivery.getDeliveryState() == DeliveryState.DELIVERED) {
            log.info("Доставка для заказа {} уже завершена", orderId);
            throw new IllegalStateException("Доставка уже завершена");
        }

        delivery.setDeliveryState(DeliveryState.DELIVERED);
        orderClient.orderDeliverySuccess(orderId);
        deliveryRepository.save(delivery);

        log.info("Доставка для заказа {} успешно завершена", orderId);
    }

    @Override
    @Transactional
    public void pickedItemToDelivery(UUID orderId) {
        log.info("Отправка заказа {} в доставку", orderId);

        Delivery delivery = findDeliveryByOrderId(orderId);

        if (delivery.getDeliveryState() == DeliveryState.DELIVERED
                || delivery.getDeliveryState() == DeliveryState.FAILED) {
            log.info("Нельзя отправить в доставку заказ {} из статуса {}", orderId, delivery.getDeliveryState());
            throw new IllegalStateException("Нельзя перевести заказ в доставку из статуса: " +
                    delivery.getDeliveryState());
        }

        UUID deliveryId = delivery.getId();
        ShippedToDeliveryRequest request = ShippedToDeliveryRequest.builder()
                .orderId(orderId)
                .deliveryId(deliveryId)
                .build();

        log.info("Отправка в warehouseClient.productInShipped: {}", request);
        warehouseClient.productInShipped(request);

        delivery.setDeliveryState(DeliveryState.IN_PROGRESS);
        orderClient.orderAssemblySuccess(orderId);
        deliveryRepository.save(delivery);

        log.info("Заказ {} переведен в доставку (IN_PROGRESS)", orderId);
    }

    @Override
    @Transactional
    public void failedDelivery(UUID orderId) {
        log.info("Попытка пометить доставку как неудачную для заказа {}", orderId);

        Delivery delivery = findDeliveryByOrderId(orderId);

        if (delivery.getDeliveryState() == DeliveryState.DELIVERED) {
            log.info("Нельзя отменить завершённую доставку для заказа {}", orderId);
            throw new IllegalStateException("Доставка уже завершена");
        }

        delivery.setDeliveryState(DeliveryState.FAILED);
        orderClient.orderDeliveryFailed(orderId);
        deliveryRepository.save(delivery);

        log.info("Доставка для заказа {} помечена как неудачная", orderId);
    }

    @Override
    public BigDecimal calculateDeliveryCost(CalculateDeliveryDto calculateDeliveryDto) {
        log.info("Расчет стоимости доставки для заказа {}", calculateDeliveryDto.getOrder().getOrderId());
        OrderDto orderDto = calculateDeliveryDto.getOrder();

        if (orderDto.getFragile() == null || orderDto.getDeliveryVolume() == null
                || orderDto.getDeliveryWeight() == null) {
            throw new IllegalStateException("Невозможно рассчитать стоимость для заказа с id: " +
                    orderDto.getOrderId() + ", для заказа не рассчитаны вес и габариты");
        }

        AddressDto deliveryAddress = calculateDeliveryDto.getToAddress();
        AddressDto warehouseAddress = calculateDeliveryDto.getFromAddress();

        BigDecimal deliveryCost = BigDecimal.valueOf(5);
        log.info("Базовая стоимость: {}", deliveryCost);

        if (warehouseAddress.getCountry().contains("ADDRESS_2")) {
            deliveryCost = deliveryCost.multiply(BigDecimal.valueOf(2));
            log.info("Стоимость увеличена из-за склада в другой стране: {}", deliveryCost);
        }

        if (orderDto.getFragile()) {
            deliveryCost = deliveryCost.add(deliveryCost.multiply(BigDecimal.valueOf(0.2)));
            log.info("Стоимость увеличена из-за хрупкости: {}", deliveryCost);
        }

        deliveryCost = deliveryCost.add(orderDto.getDeliveryWeight().multiply(BigDecimal.valueOf(0.3)));
        deliveryCost = deliveryCost.add(orderDto.getDeliveryVolume().multiply(BigDecimal.valueOf(0.2)));
        log.info("Стоимость после учета веса и объема: {}", deliveryCost);

        boolean isDifferentAddress = !deliveryAddress.getCountry().equals(warehouseAddress.getCountry())
                || !deliveryAddress.getCity().equals(warehouseAddress.getCity())
                || !deliveryAddress.getStreet().equals(warehouseAddress.getStreet());

        if (isDifferentAddress) {
            deliveryCost = deliveryCost.add(deliveryCost.multiply(BigDecimal.valueOf(0.2)));
            log.info("Стоимость увеличена из-за разных адресов: {}", deliveryCost);
        }

        BigDecimal finalCost = deliveryCost.setScale(2, RoundingMode.HALF_UP);
        log.info("Итоговая стоимость доставки для заказа {}: {}", orderDto.getOrderId(), finalCost);

        return finalCost;
    }

    private Delivery findDeliveryByOrderId(UUID orderId) {
        log.info("Поиск доставки по orderId {}", orderId);
        return deliveryRepository.findDeliveryByOrderId(orderId)
                .orElseThrow(() -> {
                    log.info("Доставка для заказа {} не найдена", orderId);
                    return new NoDeliveryFoundException("Доставка для заказа с id " + orderId + " не найдена",
                            "Доставка для заказа с id " + orderId + " не найдена");
                });
    }

    private DeliveryAddress findDeliveryAddressById(UUID deliveryId) {
        log.info("Поиск адреса доставки по deliveryId {}", deliveryId);
        return deliveryAddressRepository.findById(deliveryId)
                .orElseThrow(() -> {
                    log.info("Адрес доставки с id {} не найден", deliveryId);
                    return new NoDeliveryFoundException("Адрес доставки с id " + deliveryId + " не найден",
                            "Адрес доставки с id " + deliveryId + " не найден");
                });
    }

    private WarehouseAddress findWarehouseAddressById(UUID deliveryId) {
        log.info("Поиск адреса склада по deliveryId {}", deliveryId);
        return warehouseAddressRepository.findById(deliveryId)
                .orElseThrow(() -> {
                    log.info("Адрес склада с id {} не найден", deliveryId);
                    return new NoOrderFoundException("Адрес склада с id " + deliveryId + " не найден",
                            "Адрес склада с id " + deliveryId + " не найден");
                });
    }
}
