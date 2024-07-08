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
    private final EventProducer eventProducer;

    @Transactional
    @KafkaListener(topics = {"success-check-stock"}, groupId = "orders-service")
    public void successOrders(Message<?> message) {
        CreateOrderEventDto createOrderEventDto = null;
        try {
            createOrderEventDto = objectMapper.convertValue(message.getPayload(), new TypeReference<>() {
            });

            Orders orders = ordersRepository.save(Orders.from(createOrderEventDto));
            createOrderEventDto.setOrderId(orders.getId());
            eventProducer.sendEvent("success-create-orders", createOrderEventDto);

        } catch (Exception e) {
            log.error("Failed to update order status with ID: {}. Exception: ", Objects.requireNonNull(createOrderEventDto).getOrderId(), e);
            eventProducer.sendEvent("fail-create-orders", createOrderEventDto);
        }
    }

    @Transactional
    @KafkaListener(topics = {"success-create-orders-product"}, groupId = "orders-service")
    public void completeOrders(Message<?> message) {
        CreateOrderEventDto createOrderEventDto = null;
        try {
            createOrderEventDto = objectMapper.convertValue(message.getPayload(), new TypeReference<>() {
            });
            Orders orders = ordersRepository.findById(createOrderEventDto.getOrderId()).orElseThrow(() -> new OrdersException(ErrorCode.NOT_FOUND_ORDERS));
            orders.successOrders();
        } catch (Exception e) {
            log.error("Failed to complete orders with ID: {}. Exception: ", Objects.requireNonNull(createOrderEventDto).getOrderId(), e);
        }
    }

}
