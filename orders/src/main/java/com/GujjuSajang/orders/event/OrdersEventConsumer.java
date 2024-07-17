package com.GujjuSajang.orders.event;

import com.GujjuSajang.core.dto.CreateOrderEventDto;
import com.GujjuSajang.core.exception.ErrorCode;
import com.GujjuSajang.core.exception.OrdersException;
import com.GujjuSajang.core.type.OrdersStatus;
import com.GujjuSajang.orders.entity.Orders;
import com.GujjuSajang.orders.repository.OrdersRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrdersEventConsumer {

    private final OrdersRepository ordersRepository;
    private final ObjectMapper objectMapper;
    private final EventProducer eventProducer;

    // 결제 성공 이벤트 받아서 주문 생성
    @KafkaListener(topics = {"success-payment"})
    public void createOrders(Message<?> message) {
        CreateOrderEventDto createOrderEventDto = null;
        try {

            createOrderEventDto = objectMapper.convertValue(message.getPayload(), new TypeReference<>() {
            });
            Orders orders = ordersRepository.save(Orders.of(createOrderEventDto, OrdersStatus.COMPLETE));
            createOrderEventDto.setOrderId(orders.getId());
            eventProducer.sendEvent("created-orders", createOrderEventDto);

        } catch (Exception e) {
            log.error("Failed to update order status with ID: {}. Exception: ", Objects.requireNonNull(createOrderEventDto).getOrderId(), e);
            eventProducer.sendEvent("failed-create-orders", createOrderEventDto);
        }
    }

    // 주문 실패 처리
    @KafkaListener(topics = {"failed-create-orders-product", "failed-deduct-stock"})
    public void failOrdersFromFailedOrdersProducts(Message<?> message) {
        CreateOrderEventDto createOrderEventDto = null;
        try {
            createOrderEventDto = objectMapper.convertValue(message.getPayload(), new TypeReference<>() {
            });
            ordersRepository.setStatus(createOrderEventDto.getOrderId(), OrdersStatus.PROCESSING_ERROR);
        } catch (Exception e) {
            log.error("error change order status from fail orders products message: {}. Exception:", createOrderEventDto, e);
        }
    }

}
