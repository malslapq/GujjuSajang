package com.GujjuSajang.orders.event;

import com.GujjuSajang.core.dto.CreateOrderEventDto;
import com.GujjuSajang.core.exception.ErrorCode;
import com.GujjuSajang.core.exception.OrdersException;
import com.GujjuSajang.orders.entity.Orders;
import com.GujjuSajang.orders.repository.OrdersRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrdersEventConsumer {

    private final OrdersRepository ordersRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = {"fail-create-orders-product", "fail-check-stock"}, groupId = "orders-service")
    public void failCreateOrdersProduct(Message<?> message) {
        Long ordersId = null;
        try {
            ordersId = objectMapper.convertValue(message.getPayload(), new TypeReference<>() {
            });
            ordersRepository.deleteById(ordersId);
        } catch (Exception e) {
            log.error("Failed to delete order with ID: {}. Exception: ", ordersId, e);
        }
    }

    @KafkaListener(topics = {"reset-stock-count"}, groupId = "orders-service")
    @Transactional
    public void changeOrdersStatusFromFailCreateOrdersProduct(Message<?> message) {
        Long ordersId = null;
        try {
            ordersId = objectMapper.convertValue(message.getPayload(), new TypeReference<>() {
            });
            Orders orders = ordersRepository.findById(ordersId).orElseThrow(() -> new OrdersException(ErrorCode.NOT_FOUND_ORDERS));
            orders.failOrders();
        } catch (Exception e) {
            log.error("Failed to update order status with ID: {}. Exception: ", ordersId, e);
        }
    }

    @Transactional
    @KafkaListener(topics = {"success-check-stock"}, groupId = "orders-service")
    public void successOrders(Message<?> message) {
        CreateOrderEventDto createOrderEventDto = null;
        try {
            createOrderEventDto = objectMapper.convertValue(message.getPayload(), new TypeReference<>() {
            });
            Orders orders = ordersRepository.findById(createOrderEventDto.getOrderId()).orElseThrow(() -> new OrdersException(ErrorCode.NOT_FOUND_ORDERS));
            orders.successOrders();
        } catch (Exception e) {
            log.error("Failed to update order status with ID: {}. Exception: ", Objects.requireNonNull(createOrderEventDto).getOrderId(), e);
        }

    }

}
