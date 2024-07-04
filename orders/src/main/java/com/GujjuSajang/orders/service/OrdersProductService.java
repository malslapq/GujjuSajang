package com.GujjuSajang.orders.service;

import com.GujjuSajang.core.dto.CreateOrderEventDto;
import com.GujjuSajang.core.dto.OrdersProductDto;
import com.GujjuSajang.core.dto.UpdateOrdersProductStatusDto;
import com.GujjuSajang.core.entity.OrdersProduct;
import com.GujjuSajang.core.service.EventProducerService;
import com.GujjuSajang.core.type.OrdersStatus;
import com.GujjuSajang.orders.repository.OrdersProductRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.Message;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrdersProductService {

    private final OrdersProductRepository ordersProductRepository;
    private final EventProducerService eventProducerService;
    private final ObjectMapper objectMapper;

    @Transactional
    @KafkaListener(topics = {"create-orders"}, groupId = "createOrdersProduct")
    public void createOrdersProduct(CreateOrderEventDto createOrderEventDto) {
        List<OrdersProduct> ordersProducts = createOrderEventDto.getCartProductsDtos().stream()
                .map(cartProductsDto -> OrdersProduct.of(createOrderEventDto.getOrderId(), cartProductsDto)).toList();
        List<OrdersProductDto> ordersProductDtoList = ordersProductRepository.saveAll(ordersProducts)
                .stream()
                .map(OrdersProductDto::from)
                .toList();
        eventProducerService.sendEventWithKey("create-orders-product", "orders", ordersProductDtoList);
    }

    @Transactional
    @KafkaListener(topics = {"checkStock"}, groupId = "updateStatus")
    public void updateStatus(Message<?> message) {
        List<OrdersProductDto> ordersProductDtoList = objectMapper.convertValue(message.getPayload(), new TypeReference<>() {
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

        ordersProductRepository.saveAll(ordersProducts);
    }

    @Transactional
    @Scheduled(cron = "0 0 0 * * *")
    public void updateStatus() {
        List<OrdersStatus> statuses = Arrays.asList(OrdersStatus.COMPLETE, OrdersStatus.DELIVERY, OrdersStatus.RETURN_REQUEST);
        List<OrdersProduct> ordersProductList = ordersProductRepository.findByStatusIn(statuses);
        Map<Long, Integer> ordersProductCountMap = new HashMap<>();

        for (OrdersProduct ordersProduct : ordersProductList) {
            Long ids = ordersProduct.updateDeliveryStatus();
            if (ids != -1) {
                ordersProductCountMap.put(ids, ordersProduct.getCount());
            }
        }


        if (!ordersProductCountMap.isEmpty()) {
            List<Long> productIds = ordersProductCountMap.keySet().stream().toList();
            eventProducerService.sendEvent("update-ordersProduct-status",
                    UpdateOrdersProductStatusDto.builder()
                            .productIds(productIds)
                            .ordersProductCountsMap(ordersProductCountMap)
                            .build());
        }

    }


}
