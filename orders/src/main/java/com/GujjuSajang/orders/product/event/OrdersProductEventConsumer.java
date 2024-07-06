package com.GujjuSajang.orders.product.event;

import com.GujjuSajang.core.dto.CreateOrderEventDto;
import com.GujjuSajang.orders.product.dto.OrdersProductDto;
import com.GujjuSajang.orders.product.entity.OrdersProduct;
import com.GujjuSajang.orders.event.EventProducer;
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


    // 재고 확인 성공 이벤트 받아서 주문 제품 생성 (중간에 결제 했다고 가정)
    @Transactional
    @KafkaListener(topics = {"success-check-stock"}, groupId = "order-product-service")
    public void createOrdersProduct(Message<?> message) {
        CreateOrderEventDto createOrderEventDto = null;
        try {
            createOrderEventDto = objectMapper.convertValue(message.getPayload(), new TypeReference<>() {
            });
            CreateOrderEventDto finalCreateOrderEventDto = createOrderEventDto;
            List<OrdersProduct> ordersProducts = createOrderEventDto.getCartProductsDtos().stream()
                    .map(cartProductsDto -> OrdersProduct.of(finalCreateOrderEventDto.getOrderId(), cartProductsDto)).toList();
            List<OrdersProductDto> ordersProductDtoList = ordersProductRepository.saveAll(ordersProducts).stream().map(OrdersProductDto::from).toList();
            eventProducer.sendEvent("create-orders-product", ordersProductDtoList);
        } catch (Exception e) {
            eventProducer.sendEvent("fail-create-orders-product", createOrderEventDto);
        }
    }

    // 반품 완료 이후 재고 수정에서 실패했을 경우
    @KafkaListener(topics = {"fail-return-completed-ordersProduct"}, groupId = "order-product-service")
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
