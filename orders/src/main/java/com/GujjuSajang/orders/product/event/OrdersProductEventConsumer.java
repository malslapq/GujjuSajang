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
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrdersProductEventConsumer {

    private final OrdersProductRepository ordersProductRepository;
    private final EventProducer eventProducer;
    private final ObjectMapper objectMapper;


    // 주문 결제 성공 이벤트 받아서 주문 제품 생성
    @Transactional
    @KafkaListener(topics = {"success-create-orders"}, groupId = "orders-product-service")
    public void createOrdersProduct(Message<?> message) {
        CreateOrderEventDto createOrderEventDto = null;
        try {
            createOrderEventDto = objectMapper.convertValue(message.getPayload(), new TypeReference<>() {
            });
            CreateOrderEventDto finalCreateOrderEventDto = createOrderEventDto;
            List<OrdersProduct> ordersProducts = createOrderEventDto.getCartProductsDtos().stream()
                    .map(cartProductsDto -> OrdersProduct.of(finalCreateOrderEventDto.getOrderId(), cartProductsDto, DeliveryStatus.COMPLETE)).toList();
            ordersProductRepository.saveAll(ordersProducts);
            eventProducer.sendEvent("success-create-orders-product", createOrderEventDto);
        } catch (Exception e) {
            eventProducer.sendEvent("fail-create-orders-product", createOrderEventDto);
        }
    }

    // 주문 실패 이벤트 받아서 처리
    @Transactional
    @KafkaListener(topics = {"create-orders-from-failed-payment"}, groupId = "orders-product-service")
    public void cancelOrdersProductFromPayment(Message<?> message) {
        CreateOrderEventDto createOrderEventDto = null;
        try {
            createOrderEventDto = objectMapper.convertValue(message.getPayload(), new TypeReference<>() {
            });
            CreateOrderEventDto finalCreateOrderEventDto = createOrderEventDto;
            List<OrdersProduct> ordersProducts = createOrderEventDto.getCartProductsDtos().stream()
                    .map(cartProductsDto -> OrdersProduct.of(finalCreateOrderEventDto.getOrderId(), cartProductsDto, DeliveryStatus.CANCEL)).toList();
            ordersProductRepository.saveAll(ordersProducts);
        } catch (Exception e) {
            log.error("fail-create-orders-product order : {}", Objects.requireNonNull(createOrderEventDto).getOrderId(), e);
        }
    }

    // 재고 처리 실패 이벤트 받아서 처리
    @Transactional
    @KafkaListener(topics = {"fail-reduce-stock"}, groupId = "orders-product-service")
    public void cancelOrdersProductFromStock(Message<?> message) {
        CreateOrderEventDto createOrderEventDto = null;
        try {
            createOrderEventDto = objectMapper.convertValue(message.getPayload(), new TypeReference<>() {
            });
            ordersProductRepository.updateStatusByOrdersId(createOrderEventDto.getOrderId(), DeliveryStatus.CANCEL);
        } catch (Exception e) {
            log.error("fail-cancel-orders-product ordersId : {}", Objects.requireNonNull(createOrderEventDto).getOrderId(), e);
        }
    }

    // 반품 완료 이후 재고 수정에서 실패했을 경우
    @KafkaListener(topics = {"fail-return-completed-ordersProduct"}, groupId = "orders-product-service")
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
