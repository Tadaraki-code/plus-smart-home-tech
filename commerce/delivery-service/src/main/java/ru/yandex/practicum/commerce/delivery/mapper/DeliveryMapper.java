package ru.yandex.practicum.commerce.delivery.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.commerce.delivery.model.Delivery;
import ru.yandex.practicum.commerce.delivery.model.DeliveryAddress;
import ru.yandex.practicum.commerce.delivery.model.WarehouseAddress;
import ru.yandex.practicum.commerce.interactionapi.delivery.DeliveryDto;
import ru.yandex.practicum.commerce.interactionapi.warehouse.AddressDto;

import java.util.UUID;

@Component
public class DeliveryMapper {

    public DeliveryDto toDto(Delivery delivery, DeliveryAddress toAddress,
                             WarehouseAddress fromAddress) {
        return DeliveryDto.builder()
                .deliveryId(delivery.getId())
                .orderId(delivery.getOrderId())
                .deliveryState(delivery.getDeliveryState())
                .fromAddress(deliveryAddressToDto(toAddress))
                .toAddress(warehouseAddressToDto(fromAddress))
                .build();
    }

    public Delivery fromDto(DeliveryDto deliveryDto, UUID deliveryId) {
        return Delivery.builder()
                .id(deliveryId)
                .orderId(deliveryDto.getOrderId())
                .build();
    }

    private AddressDto deliveryAddressToDto(DeliveryAddress toAddress) {
        return AddressDto.builder()
                .country(toAddress.getCountry())
                .city(toAddress.getCity())
                .street(toAddress.getStreet())
                .house(toAddress.getHouse())
                .flat(toAddress.getFlat())
                .build();
    }

    private AddressDto warehouseAddressToDto(WarehouseAddress fromAddress) {
        return AddressDto.builder()
                .country(fromAddress.getCountry())
                .city(fromAddress.getCity())
                .street(fromAddress.getStreet())
                .house(fromAddress.getHouse())
                .flat(fromAddress.getFlat())
                .build();
    }

    public DeliveryAddress toDeliveryAddress(AddressDto toAddress, UUID deliveryId) {
        return DeliveryAddress.builder()
                .deliveryId(deliveryId)
                .country(toAddress.getCountry())
                .city(toAddress.getCity())
                .street(toAddress.getStreet())
                .house(toAddress.getHouse())
                .flat(toAddress.getFlat())
                .build();
    }

    public WarehouseAddress toWarehouseAddress(AddressDto fromAddress, UUID deliveryId) {
        return WarehouseAddress.builder()
                .deliveryId(deliveryId)
                .country(fromAddress.getCountry())
                .city(fromAddress.getCity())
                .street(fromAddress.getStreet())
                .house(fromAddress.getHouse())
                .flat(fromAddress.getFlat())
                .build();
    }
}
