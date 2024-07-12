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
    @KafkaListener(topics = {"success-payment"}, groupId = "orders-service")
    public void successOrders(Message<?> message) {
        CreateOrderEventDto createOrderEventDto = null;
        try {

            createOrderEventDto = objectMapper.convertValue(message.getPayload(), new TypeReference<>() {
            });
            Orders orders = ordersRepository.save(Orders.of(createOrderEventDto, OrdersStatus.COMPLETE));
            createOrderEventDto.setOrderId(orders.getId());
            ordersRepository.save(orders);
            eventProducer.sendEvent("success-create-orders", createOrderEventDto);

        } catch (Exception e) {
            log.error("Failed to update order status with ID: {}. Exception: ", Objects.requireNonNull(createOrderEventDto).getOrderId(), e);
            eventProducer.sendEvent("fail-create-orders", createOrderEventDto);
        }
    }

    // 결제 실패 이벤트 받아서 주문 실패 처리
    @KafkaListener(topics = {"failed-payment"}, groupId = "orders-service")
    public void failOrdersFromFailedPayment(Message<?> message) {
        CreateOrderEventDto createOrderEventDto = null;
        try {
            createOrderEventDto = objectMapper.convertValue(message.getPayload(), new TypeReference<>() {
            });
            Orders orders = ordersRepository.save(Orders.of(createOrderEventDto, OrdersStatus.FAILED_PAYMENT));
            createOrderEventDto.setOrderId(orders.getId());
            ordersRepository.save(orders);
            eventProducer.sendEvent("create-orders-from-failed-payment", createOrderEventDto);

        } catch (Exception e) {
            log.error("fail orders from fail payment message: {}. Exception:", createOrderEventDto, e);
        }
    }

    // 재고 처리 실패 이벤트 받아서 주문 실패 처리
    @KafkaListener(topics = {"fail-reduce-stock"}, groupId = "orders-service")
    public void failOrdersFromFailedReduceStock(Message<?> message) {
        CreateOrderEventDto createOrderEventDto = null;
        try {
            createOrderEventDto = objectMapper.convertValue(message.getPayload(), new TypeReference<>() {
            });
            Orders orders = ordersRepository.findById(createOrderEventDto.getOrderId()).orElseThrow(() -> new OrdersException(ErrorCode.NOT_FOUND_ORDERS));
            orders.failOrdersFromReduceStock();
            ordersRepository.save(orders);
        } catch (Exception e) {
            log.error("fail orders from fail reduce stock message: {}. Exception:", createOrderEventDto, e);
        }
    }

    // 제품 상세 생성 실패 이벤트 받아서 주문 실패 처리
    @KafkaListener(topics = {"fail-create-orders-product"}, groupId = "orders-service")
    public void failOrdersFromFailedOrdersProducts(Message<?> message) {
        CreateOrderEventDto createOrderEventDto = null;
        try {
            createOrderEventDto = objectMapper.convertValue(message.getPayload(), new TypeReference<>() {
            });
            Orders orders = getOrders(createOrderEventDto);
            orders.failOrdersFromOrdersProducts();
            ordersRepository.save(orders);
        } catch (Exception e) {
            log.error("error change order status from fail orders products message: {}. Exception:", createOrderEventDto, e);
        }
    }

    private Orders getOrders(CreateOrderEventDto createOrderEventDto) {
        return ordersRepository.findById(createOrderEventDto.getOrderId()).orElseThrow(() -> new OrdersException(ErrorCode.NOT_FOUND_ORDERS));
    }

}
