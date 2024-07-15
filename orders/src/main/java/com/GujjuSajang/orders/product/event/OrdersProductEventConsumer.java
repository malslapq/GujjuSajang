package com.GujjuSajang.orders.product.event;

import com.GujjuSajang.core.dto.CreateOrderEventDto;
import com.GujjuSajang.core.type.DeliveryStatus;
import com.GujjuSajang.orders.event.EventProducer;
import com.GujjuSajang.orders.product.entity.OrdersProduct;
import com.GujjuSajang.orders.repository.OrdersProductRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrdersProductEventConsumer {

    private final OrdersProductRepository ordersProductRepository;
    private final EventProducer eventProducer;
    private final ObjectMapper objectMapper;


    // 주문 생성 성공 이벤트 받아서 주문 상세 생성
    @Transactional
    @KafkaListener(topics = {"created-orders"})
    public void createOrdersProduct(Message<?> message) {
        CreateOrderEventDto createOrderEventDto = null;
        try {
            createOrderEventDto = objectMapper.convertValue(message.getPayload(), new TypeReference<>() {
            });
            CreateOrderEventDto finalCreateOrderEventDto = createOrderEventDto;
            List<OrdersProduct> ordersProducts = createOrderEventDto.getCartProductsDtos().stream()
                    .map(cartProductsDto -> OrdersProduct.of(finalCreateOrderEventDto.getOrderId(), cartProductsDto, DeliveryStatus.COMPLETE)).toList();
            ordersProductRepository.saveAll(ordersProducts);
            eventProducer.sendEvent("created-orders-product", createOrderEventDto);
        } catch (Exception e) {
            eventProducer.sendEvent("failed-create-orders-product", createOrderEventDto);
        }
    }

    @KafkaListener(topics = {"failed-deduct-stock"})
    @Transactional
    public void cancelOrdersProduct(Message<?> message) {
        CreateOrderEventDto createOrderEventDto = null;
        try {
            createOrderEventDto = objectMapper.convertValue(message.getPayload(), new TypeReference<>() {
            });

            ordersProductRepository.updateStatusByOrdersId(createOrderEventDto.getOrderId(), DeliveryStatus.CANCEL);

        } catch (Exception e) {
            log.error("failed to cancel orders product message : {}", message, e);
        }
    }

    // 반품 완료 이후 재고 수정에서 실패했을 경우
    @Transactional
    @KafkaListener(topics = {"fail-return-completed-ordersProduct"})
    public void failReturnCompletedOrdersProduct(Message<?> message) {
        try {
            List<Long> ordersProductIds = objectMapper.convertValue(message.getPayload(), new TypeReference<>() {
            });
            ordersProductRepository.findAllById(ordersProductIds).forEach(OrdersProduct::rollBackReturnCompletedStatus);
        } catch (Exception e) {
            log.error("Failed to return completed orders product failure for message: {}", message, e);
        }

    }


}
