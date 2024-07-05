package com.GujjuSajang.orders.event;

import com.GujjuSajang.core.dto.CreateOrderEventDto;
import com.GujjuSajang.orders.dto.OrdersProductDto;
import com.GujjuSajang.orders.entity.OrdersProduct;
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
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrdersProductEventConsumer {

    private final OrdersProductRepository ordersProductRepository;
    private final EventProducer eventProducer;
    private final ObjectMapper objectMapper;


    // 주문 생성 이벤트 받아서 주문 제품 생성
    @Transactional
    @KafkaListener(topics = {"create-orders"}, groupId = "createOrdersProduct")
    public void createOrdersProduct(CreateOrderEventDto createOrderEventDto) {
        try {
            List<OrdersProduct> ordersProducts = createOrderEventDto.getCartProductsDtos().stream()
                    .map(cartProductsDto -> OrdersProduct.of(createOrderEventDto.getOrderId(), cartProductsDto)).toList();
            List<OrdersProductDto> ordersProductDtoList = ordersProductRepository.saveAll(ordersProducts)
                    .stream()
                    .map(OrdersProductDto::from)
                    .toList();
            eventProducer.sendEventWithKey("create-orders-product", "orders", ordersProductDtoList);
        } catch (Exception e) {
            eventProducer.sendEvent("fail-create-orders-product", createOrderEventDto.getOrderId());
        }
    }

    /*
     * 주문 생성 이벤트 -> 주문 제품 생성 이벤트 -> 재고 확인, 업데이트 이벤트 받아서
     * 주문 제품들 상태 변경 (주문 실패(재고 없음) or 주문 완료)
     * */
    @Transactional
    @KafkaListener(topics = {"create-orders-checkStock"}, groupId = "updateStatus")
    public void updateStatus(Message<?> message) {
        List<OrdersProductDto> ordersProductDtoList = null;
        try {
            ordersProductDtoList = objectMapper.convertValue(message.getPayload(), new TypeReference<>() {
            });
            List<Long> orderProductIds = ordersProductDtoList.stream()
                    .map(OrdersProductDto::getId)
                    .collect(Collectors.toList());


            List<OrdersProduct> ordersProducts = ordersProductRepository.findAllById(orderProductIds);

            Map<Long, OrdersProductDto> ordersProductDtoListMap = ordersProductDtoList.stream()
                    .collect(Collectors.toMap(OrdersProductDto::getId, dto -> dto));

            ordersProducts.forEach(ordersProduct -> {
                OrdersProductDto ordersProductDto = ordersProductDtoListMap.get(ordersProduct.getId());
                if (ordersProductDto != null) {
                    ordersProduct.changeStatus(ordersProductDto.getStatus());
                }
            });
        } catch (Exception e) {
            log.error("Failed to update order product status for message: {}", message, e);
            List<Long> productIds = Objects.requireNonNull(ordersProductDtoList).stream().map(OrdersProductDto::getProductId).toList();
            eventProducer.sendEvent("fail-update-status", productIds);
        }
    }

    // 주문 생성 후 주문한 제품들 상태변경에 실패했을 경우 주문실패로 처리
    @Transactional
    @KafkaListener(topics = {"fail-check-stock", "fail-update-status"}, groupId = "processingError")
    public void processingErrorStatus(Message<?> message) {
        try {
            List<Long> ordersProductIds = objectMapper.convertValue(message.getPayload(), new TypeReference<>() {
            });
            ordersProductRepository.findAllById(ordersProductIds).forEach(OrdersProduct::processingErrorStatus);
        } catch (Exception e) {
            log.error("Failed to update order product status for message: {}", message, e);
        }
    }

    // 반품 완료 이후 재고 수정에서 실패했을 경우

    @KafkaListener(topics = {"fail-return-completed-ordersProduct"}, groupId = "failReturnCompleted")
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
