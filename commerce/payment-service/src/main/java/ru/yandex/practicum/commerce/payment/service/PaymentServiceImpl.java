package ru.yandex.practicum.commerce.payment.service;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.commerce.interactionapi.clients.OrderClient;
import ru.yandex.practicum.commerce.interactionapi.clients.ShoppingStoreClient;
import ru.yandex.practicum.commerce.interactionapi.exceptions.NoOrderFoundException;
import ru.yandex.practicum.commerce.interactionapi.exceptions.NotEnoughInfoInOrderToCalculateException;
import ru.yandex.practicum.commerce.interactionapi.order.OrderDto;
import ru.yandex.practicum.commerce.interactionapi.payment.PaymentDto;
import ru.yandex.practicum.commerce.interactionapi.payment.PaymentState;
import ru.yandex.practicum.commerce.interactionapi.shop.ProductDto;
import ru.yandex.practicum.commerce.payment.mapper.PaymentMapper;
import ru.yandex.practicum.commerce.payment.model.Payment;
import ru.yandex.practicum.commerce.payment.storage.PaymentRepository;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private static final BigDecimal PAYMENT_FEE_RATE = BigDecimal.valueOf(0.1);

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final OrderClient orderClient;
    private final ShoppingStoreClient shoppingStoreClient;

    @Override
    public PaymentDto payment(OrderDto order) {
        log.info("Начинаем оплату для заказа id: {}", order.getOrderId());
        if (order.getPaymentId() != null && paymentRepository.existsById(order.getPaymentId())) {
            log.info("Оплата уже запущена для заказа id: {}", order.getOrderId());
            throw new IllegalStateException("Для заказа с id: " + order.getOrderId() +
                    " уже запущена процедура оплаты");
        }
        Payment payment = createPaymentEntity(order);
        paymentRepository.save(payment);
        log.info("Оплата создана для заказа id: {}, paymentId: {}", order.getOrderId(), payment.getId());
        return paymentMapper.toDto(payment);
    }

    @Override
    public BigDecimal calculateTotalCost(OrderDto orderDto) {
        log.info("Расчет общей стоимости для заказа id: {}", orderDto.getOrderId());
        if (orderDto.getDeliveryPrice() == null) {
            log.info("Не рассчитана стоимость доставки для заказа id: {}", orderDto.getOrderId());
            throw new NotEnoughInfoInOrderToCalculateException("Невозможно рассчитать общую стоимость для заказа с id: " +
                    orderDto.getOrderId() + ", не рассчитана стоимость доставки.",
                    "Невозможно рассчитать общую стоимость для заказа с id: " +
                            orderDto.getOrderId() + ", не рассчитана стоимость доставки.");
        }
        BigDecimal totalCost = orderDto.getProductPrice()
                .add(orderDto.getDeliveryPrice())
                .add(orderDto.getProductPrice().multiply(PAYMENT_FEE_RATE));
        log.info("Общая стоимость заказа id: {} рассчитана: {}", orderDto.getOrderId(), totalCost);
        return totalCost;
    }

    @Override
    public BigDecimal calculateProductCost(OrderDto orderDto) {
        log.info("Расчет стоимости продуктов для заказа id: {}", orderDto.getOrderId());
        if (orderDto.getProducts().isEmpty()) {
            log.info("Список продуктов пуст для заказа id: {}", orderDto.getOrderId());
            throw new NotEnoughInfoInOrderToCalculateException("Невозможно рассчитать стоимость продуктов для заказа с id: " +
                    orderDto.getOrderId() + ", так как список товаров пуст.",
                    "Невозможно рассчитать стоимость продуктов для заказа с id: " +
                            orderDto.getOrderId() + ", так как список товаров пуст.");
        }

        BigDecimal totalProductCost = BigDecimal.ZERO;

        for (Map.Entry<UUID, Integer> entry : orderDto.getProducts().entrySet()) {
            ProductDto productDto = shoppingStoreClient.getProduct(entry.getKey());
            BigDecimal productCost = productDto.getPrice().multiply(BigDecimal.valueOf(entry.getValue()));
            totalProductCost = totalProductCost.add(productCost);
        }

        log.info("Общая стоимость продуктов для заказа id: {} рассчитана: {}", orderDto.getOrderId(), totalProductCost);
        return totalProductCost;
    }

    @Override
    @Transactional
    public void paymentFailed(UUID paymentId) {
        log.info("Обработка статуса FAILED для платежа id: {}", paymentId);
        Payment payment = getPaymentById(paymentId);
        if (payment.getPaymentState() != PaymentState.PENDING) {
            log.info("Нельзя перевести платеж id: {} в FAILED из состояния: {}", paymentId, payment.getPaymentState());
            throw new IllegalStateException("Нельзя перевести оплату с id: " + paymentId +
                    " в FAILED из состояния: " + payment.getPaymentState());
        }
        payment.setPaymentState(PaymentState.FAILED);
        paymentRepository.save(payment);
        log.info("Платеж id: {} переведен в FAILED", paymentId);
        orderClient.orderPaymentFailed(payment.getOrderId());
    }

    @Override
    @Transactional
    public void paymentSuccess(UUID paymentId) {
        log.info("Обработка статуса SUCCESS для платежа id: {}", paymentId);
        Payment payment = getPaymentById(paymentId);
        if (payment.getPaymentState() != PaymentState.PENDING) {
            log.warn("Нельзя завершить платеж id: {} из состояния: {}", paymentId, payment.getPaymentState());
            throw new IllegalStateException("Нельзя завершить оплату с id: " + paymentId +
                    " из состояния: " + payment.getPaymentState());
        }
        payment.setPaymentState(PaymentState.SUCCESS);
        paymentRepository.save(payment);
        log.info("Платеж id: {} успешно завершен", paymentId);
        System.out.println(payment.getOrderId());
        orderClient.orderPaymentSuccess(payment.getOrderId());
    }

    @Override
    public void refund(UUID paymentId) {
        log.info("Запрос на возврат платежа id: {}", paymentId);
        Payment payment = getPaymentById(paymentId);
        if (payment.getOrderId() == null) {
            log.error("Заказ для возврата средств не найден, id оплаты: {}", paymentId);
            throw new NoOrderFoundException("Заказ для возврата средств не найден, id оплаты: " + payment,
                    "Заказ для возврата средств не найден, id оплаты: " + payment);
        }
        if (payment.getPaymentState().equals(PaymentState.SUCCESS)) {
            log.info("Возвращаем деньги {} за заказ с id {}", payment.getTotalPayment(), payment.getOrderId());
        }
        if (payment.getPaymentState().equals(PaymentState.PENDING)) {
            log.info("Отправляем запрос в платёжный сервис на отмену оплаты на сумму {} за заказ с id {}",
                    payment.getTotalPayment(), payment.getOrderId());
        }
    }

    private Payment createPaymentEntity(OrderDto order) {
        log.info("Создание сущности Payment для заказа id: {}", order.getOrderId());
        if (order.getDeliveryPrice() == null || order.getProductPrice() == null || order.getTotalPrice() == null) {
            log.info("Не рассчитана стоимость оплаты для заказа id: {}", order.getOrderId());
            throw new IllegalStateException("Не рассчитана стоимость оплаты для заказа с id: " + order.getOrderId());
        }

        BigDecimal fee = order.getProductPrice().multiply(PAYMENT_FEE_RATE);

        return Payment.builder()
                .id(UUID.randomUUID())
                .orderId(order.getOrderId())
                .paymentState(PaymentState.PENDING)
                .totalPayment(order.getTotalPrice())
                .deliveryTotal(order.getDeliveryPrice())
                .feeTotal(fee)
                .build();
    }

    private Payment getPaymentById(UUID paymentId) {
        log.info("Поиск платежа по id: {}", paymentId);
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> {
                    log.info("Платеж с id {} не найден", paymentId);
                    return new IllegalStateException("Оплата с id " + paymentId + " не найдена");
                });
    }
}