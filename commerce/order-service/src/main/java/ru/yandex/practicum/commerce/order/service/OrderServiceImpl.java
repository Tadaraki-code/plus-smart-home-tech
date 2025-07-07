package ru.yandex.practicum.commerce.order.service;

import org.springframework.transaction.annotation.Transactional;
import jakarta.ws.rs.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.commerce.interactionapi.warehouse.AddressDto;
import ru.yandex.practicum.commerce.interactionapi.clients.DeliveryClient;
import ru.yandex.practicum.commerce.interactionapi.clients.PaymentClient;
import ru.yandex.practicum.commerce.interactionapi.clients.WarehouseClient;
import ru.yandex.practicum.commerce.interactionapi.delivery.CalculateDeliveryDto;
import ru.yandex.practicum.commerce.interactionapi.delivery.DeliveryDto;
import ru.yandex.practicum.commerce.interactionapi.delivery.DeliveryState;
import ru.yandex.practicum.commerce.interactionapi.exceptions.NoOrderFoundException;
import ru.yandex.practicum.commerce.interactionapi.order.CreateNewOrderRequest;
import ru.yandex.practicum.commerce.interactionapi.order.OrderDto;
import ru.yandex.practicum.commerce.interactionapi.order.OrderState;
import ru.yandex.practicum.commerce.interactionapi.order.ProductReturnRequest;
import ru.yandex.practicum.commerce.interactionapi.payment.PaymentDto;
import ru.yandex.practicum.commerce.interactionapi.payment.PaymentState;
import ru.yandex.practicum.commerce.interactionapi.warehouse.AssemblyProductsForOrderRequest;
import ru.yandex.practicum.commerce.interactionapi.warehouse.BookedProductsDto;
import ru.yandex.practicum.commerce.order.mapper.OrderMapper;
import ru.yandex.practicum.commerce.order.model.Order;
import ru.yandex.practicum.commerce.order.model.OrderAddress;
import ru.yandex.practicum.commerce.order.storage.OrderRepository;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final DeliveryClient deliveryClient;
    private final PaymentClient paymentClient;
    private final WarehouseClient warehouseClient;

    @Override
    public Page<OrderDto> getUserOrders(String username, Pageable pageable) {
        log.info("Получение заказов для пользователя: {}", username);
        Page<Order> productPage = orderRepository.findByUsername(username, pageable);
        log.info("Найдено {} заказов для пользователя {}", productPage.getTotalElements(), username);
        return productPage.map(orderMapper::toDto);
    }

    @Override
    @Transactional
    public OrderDto createNewOrder(CreateNewOrderRequest orderRequest) {
        log.info("Создание нового заказа для пользователя: {}", orderRequest.getUsername());
        Order order = Order.builder()
                .id(UUID.randomUUID())
                .shoppingCartId(orderRequest.getShoppingCartDto().getShoppingCartId())
                .products(orderRequest.getShoppingCartDto().getProducts())
                .username(orderRequest.getUsername())
                .state(OrderState.NEW)
                .build();

        OrderAddress address = OrderAddress.builder()
                .orderId(order.getId())
                .country(orderRequest.getAddressDto().getCountry())
                .city(orderRequest.getAddressDto().getCity())
                .street(orderRequest.getAddressDto().getStreet())
                .house(orderRequest.getAddressDto().getHouse())
                .flat(orderRequest.getAddressDto().getFlat())
                .order(order)
                .build();

        order.setAddress(address);
        orderRepository.save(order);

        log.info("Новый заказ создан с id: {}", order.getId());
        return orderMapper.toDto(order);
    }

    @Override
    public OrderDto returnOrder(ProductReturnRequest returnRequest) {
        UUID orderId = returnRequest.getOrderId();
        log.info("Запуск отмены или возврата заказа с id {}", orderId);

        Order order = findOrderById(orderId);
        log.info("Текущее состояние заказа с id {}: {}", orderId, order.getState());

        if (!(order.getState().equals(OrderState.NEW) || order.getState().equals(OrderState.PAYMENT_FAILED))) {
            log.info("Заказ с id {} не в состоянии NEW или PAYMENT_FAILED, инициируем возврат оплаты", orderId);
            paymentClient.refund(order.getPaymentId());
        } else {
            log.info("Для заказа с id {} возврат оплаты не требуется", orderId);
        }

        if (order.getState().equals(OrderState.ASSEMBLED) || order.getState().equals(OrderState.ON_DELIVERY)
                || order.getState().equals(OrderState.DELIVERED)) {
            log.info("Заказ с id {} находится в состоянии {}, выполняем возврат товаров на склад",
                    orderId, order.getState());
            warehouseClient.returnProductInWarehouse(returnRequest.getProducts(), orderId);
        } else {
            log.info("Заказ с id {} не требует возврата товаров на склад (состояние: {})",
                    orderId, order.getState());
        }

        order.setState(OrderState.CANCELED);
        Order savedOrder = orderRepository.save(order);
        log.info("Заказ с id {} успешно переведён в состояние CANCELED", orderId);

        return orderMapper.toDto(savedOrder);
    }

    @Override
    @Transactional
    public OrderDto orderPayment(UUID orderId) {
        log.info("Попытка провести оплату для заказа: {}", orderId);
        Order order = findOrderById(orderId);
        if (!order.getState().equals(OrderState.NEW)) {
            log.info("Попытка оплатить заказ в состоянии: {}", order.getState());
        }

        if (order.getDeliveryPrice() == null || order.getTotalPrice() == null) {
            log.info("У заказа {} не рассчитана цена доставки или итоговая стоимость", orderId);
            throw new IllegalStateException("Невозможно провести оплату без цен");
        }

        log.info("Отправка запроса на оплату заказа: {}", orderId);
        PaymentDto payment = paymentClient.payment(orderMapper.toDto(order));
        log.info("Ответ на оплату заказа {}: {}", orderId, payment.getPaymentState());

        if (payment.getPaymentState() == PaymentState.PENDING) {
            order.setState(OrderState.ON_PAYMENT);
            order.setPaymentId(payment.getPaymentId());
            log.info("Заказ {} переведен в состояние ON_PAYMENT", orderId);
        } else {
            order.setState(OrderState.PAYMENT_PROBLEMS);
            log.info("Проблема с оплатой заказа {}", orderId);
        }

        return orderMapper.toDto(orderRepository.save(order));
    }

    @Override
    public void orderPaymentSuccess(UUID orderId) {
        log.info("Отмечаем успешную оплату для заказа: {}", orderId);
        Order order = findOrderById(orderId);
        if (!order.getState().equals(OrderState.ON_PAYMENT)) {
            log.info("Попытка подтвердить оплату для заказа {} в состоянии {}", orderId, order.getState());
            throw new BadRequestException("Неверное состояние для подтверждения оплаты");
        }
        order.setState(OrderState.PAID);
        orderRepository.save(order);
        log.info("Заказ {} оплачен успешно", orderId);
    }

    @Override
    public OrderDto orderPaymentFailed(UUID orderId) {
        log.info("Отмечаем провал оплаты для заказа: {}", orderId);
        Order order = findOrderById(orderId);
        OrderState state = order.getState();
        if (state.equals(OrderState.ON_PAYMENT)) {
            order.setState(OrderState.PAYMENT_FAILED);
            log.info("Заказ {} не оплачен", orderId);
            return orderMapper.toDto(orderRepository.save(order));
        }
        log.info("Попытка записать провальный статус оплаты для заказа {} в состоянии {}",
                orderId, order.getState());
        throw new BadRequestException("Для заказ c id: " + orderId + " ещё не проводилась оплата " +
                "или возникла ошибка при её проведении");
    }

    @Override
    public OrderDto orderDelivery(UUID orderId) {
        log.info("Оформляем доставку для заказа: {}", orderId);
        Order order = findOrderById(orderId);
        if (!order.getState().equals(OrderState.PAID)) {
            log.info("Попытка оформить доставку для заказа {} в состоянии {}", orderId, order.getState());
            throw new IllegalStateException("Заказ не оплачен");
        }

        AddressDto warehouse = warehouseClient.getWarehouseAddress();
        AddressDto fromAddress = getFromAddress(order.getAddress());
        DeliveryDto delivery = DeliveryDto.builder()
                .toAddress(fromAddress)
                .fromAddress(warehouse)
                .orderId(orderId)
                .build();

        log.info("Отправляем запрос на создание доставки для заказа {}", orderId);
        DeliveryDto fullDeliveryInfo = deliveryClient.createDelivery(delivery);
        log.info("Ответ от сервиса доставки для заказа {}: {}", orderId, fullDeliveryInfo.getDeliveryState());

        order.setDeliveryId(fullDeliveryInfo.getDeliveryId());
        if (fullDeliveryInfo.getDeliveryState().equals(DeliveryState.CREATED)) {
            order.setState(OrderState.ON_DELIVERY);
            log.info("Заказ {} передан в доставку", orderId);
        } else {
            order.setState(OrderState.DELIVERY_PROBLEMS);
            log.info("Проблема при создании доставки для заказа {}", orderId);
        }

        return orderMapper.toDto(orderRepository.save(order));
    }

    @Override
    public void orderDeliverySuccess(UUID orderId) {
        log.info("Попытка завершить доставку для заказа с id: {}", orderId);
        Order order = findOrderById(orderId);
        OrderState state = order.getState();

        if (state.equals(OrderState.ASSEMBLED)) {
            order.setState(OrderState.DELIVERED);
            orderRepository.save(order);
            log.info("Доставка успешно завершена для заказа с id: {}", orderId);
        } else {
            log.info("Невозможно завершить доставку для заказа с id: {}, текущий статус: {}", orderId, state);
        }
    }

    @Override
    public OrderDto orderDeliveryFailed(UUID orderId) {
        log.info("Попытка пометить доставку как проваленную для заказа с id: {}", orderId);
        Order order = findOrderById(orderId);
        OrderState state = order.getState();

        if (!state.equals(OrderState.DELIVERED)) {
            order.setState(OrderState.DELIVERY_FAILED);
            orderRepository.save(order);
            log.info("Доставка провалена для заказа с id: {}", orderId);
        } else {
            log.info("Невозможно пометить доставку как проваленную для заказа с id: {}, " +
                    "так как заказ уже в статусе: {}", orderId, state);
        }

        return orderMapper.toDto(order);
    }

    @Override
    public OrderDto orderCompleted(UUID orderId) {
        log.info("Попытка завершить заказ с id: {}", orderId);
        Order order = findOrderById(orderId);
        OrderState state = order.getState();

        if (state.equals(OrderState.DELIVERED)) {
            order.setState(OrderState.COMPLETED);
            orderRepository.save(order);
            log.info("Заказ с id: {} успешно завершён", orderId);
        } else {
            log.warn("Невозможно завершить заказ с id: {}, текущий статус: {}", orderId, state);
        }

        return orderMapper.toDto(order);
    }

    @Override
    public OrderDto calculateTotalOrderCost(UUID orderId) {
        log.info("Расчет общей стоимости заказа: {}", orderId);
        Order order = findOrderById(orderId);
        if (!order.getState().equals(OrderState.NEW) || order.getTotalPrice() != null) {
            log.info("Попытка повторного расчета общей цены для заказа {}", orderId);
            throw new IllegalStateException("Цена уже рассчитана");
        }

        if (order.getDeliveryPrice() == null) {
            log.info("Попытка расчета без цены доставки для заказа {}", orderId);
            throw new IllegalStateException("Нет цены доставки");
        }

        log.info("Запрашиваем стоимость товаров для заказа {}", orderId);
        BigDecimal productPrice = paymentClient.calculateProductCost(orderMapper.toDto(order));
        order.setProductPrice(productPrice);
        BigDecimal totalPrice = paymentClient.calculateTotalCost(orderMapper.toDto(order));
        order.setTotalPrice(totalPrice);

        log.info("Цена заказа {} рассчитана: товаров на {}, итого с доставкой {}", orderId, productPrice, totalPrice);

        return orderMapper.toDto(orderRepository.save(order));
    }

    @Override
    public OrderDto calculateDeliveryOrderCost(UUID orderId) {
        log.info("Начинаем расчет стоимости доставки для заказа id: {}", orderId);
        Order order = findOrderById(orderId);
        OrderState state = order.getState();

        if (!state.equals(OrderState.NEW) || order.getDeliveryPrice() != null) {
            log.error("Невозможно рассчитать стоимость доставки для заказа id: {}, статус: {}, доставка уже расчитана: {}",
                    orderId, state, order.getDeliveryPrice() != null);
            throw new IllegalStateException("Для заказа уже расчитна цена доставки");
        }

        AddressDto warehouse = warehouseClient.getWarehouseAddress();
        System.out.println(order);
        AddressDto toAddress = getFromAddress(order.getAddress());
        CalculateDeliveryDto calculateDeliveryDto = CalculateDeliveryDto.builder()
                .order(orderMapper.toDto(order))
                .fromAddress(warehouse)
                .toAddress(toAddress)
                .build();

        BigDecimal deliveryPrice = deliveryClient.calculateDeliveryCost(calculateDeliveryDto);
        order.setDeliveryPrice(deliveryPrice);
        Order savedOrder = orderRepository.save(order);
        log.info("Стоимость доставки для заказа id: {} успешно расчитана: {}", orderId, deliveryPrice);
        return orderMapper.toDto(savedOrder);
    }

    @Override
    public OrderDto orderAssembly(UUID orderId) {
        log.info("Начинаем сборку заказа id: {}", orderId);
        Order order = findOrderById(orderId);
        OrderState state = order.getState();

        if (state.equals(OrderState.NEW)) {
            AssemblyProductsForOrderRequest request = AssemblyProductsForOrderRequest.builder()
                    .products(order.getProducts())
                    .username(order.getUsername())
                    .orderId(orderId)
                    .build();

            BookedProductsDto bookedProducts = warehouseClient.assemblyProductForDelivery(request);
            order.setFragile(bookedProducts.getFragile());
            order.setDeliveryWeight(bookedProducts.getDeliveryWeight());
            order.setDeliveryVolume(bookedProducts.getDeliveryVolume());

            log.info("Заказ id: {} собран. Хрупкий: {}, Вес: {}, Объем: {}",
                    orderId, bookedProducts.getFragile(),
                    bookedProducts.getDeliveryWeight(), bookedProducts.getDeliveryVolume());
        } else {
            log.info("Заказ id: {} не в состоянии NEW, сборка пропущена, текущий статус: {}", orderId, state);
        }

        Order savedOrder = orderRepository.save(order);
        return orderMapper.toDto(savedOrder);
    }

    @Override
    public void orderAssemblySuccess(UUID orderId) {
        log.info("Помечаем заказ id: {} как успешно собранный", orderId);
        Order order = findOrderById(orderId);
        order.setState(OrderState.ASSEMBLED);
        orderRepository.save(order);
    }

    @Override
    public OrderDto orderAssemblyFailed(UUID orderId) {
        log.info("Помечаем заказ id: {} как с ошибкой сборки", orderId);
        Order order = findOrderById(orderId);
        OrderState state = order.getState();

        if (state.equals(OrderState.PAID)) {
            order.setState(OrderState.ASSEMBLY_FAILED);
            log.info("Заказ id: {} переведен в статус ASSEMBLY_FAILED", orderId);
        } else {
            log.info("Заказ id: {} не в статусе PAID, сборка не помечена как проваленная, текущий статус: {}",
                    orderId, state);
        }

        Order savedOrder = orderRepository.save(order);
        return orderMapper.toDto(savedOrder);
    }

    private Order findOrderById(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new NoOrderFoundException("Заказ с id: " + orderId + " не найден",
                        "Заказ с id: " + orderId + " не найден"));
    }

    private AddressDto getFromAddress(OrderAddress fromAddress) {
        return AddressDto.builder()
                .country(fromAddress.getCountry())
                .city(fromAddress.getCity())
                .street(fromAddress.getStreet())
                .house(fromAddress.getHouse())
                .flat(fromAddress.getFlat())
                .build();
    }
}
