package com.GujjuSajang.orders.event;

import com.GujjuSajang.orders.repository.OrdersRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrdersEventConsumer {

    private final OrdersRepository ordersRepository;
    private static final Logger logger = LoggerFactory.getLogger(OrdersEventConsumer.class);
    private final ObjectMapper objectMapper;


    @KafkaListener(topics = {"fail-create-orders-product"}, groupId = "failCreateOrdersProduct")
    public void failCreateOrdersProduct(Message<?> message) {
        try {
            Long ordersId = objectMapper.convertValue(message.getPayload(), new TypeReference<>() {
            });
            ordersRepository.deleteById(ordersId);
        } catch (Exception e) {
            logger.error("Failed to delete order with ID: {}. Exception: ", message, e);
        }


    }

}
