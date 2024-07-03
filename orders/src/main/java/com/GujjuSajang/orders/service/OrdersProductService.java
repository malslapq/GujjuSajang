package com.GujjuSajang.orders.service;

import com.GujjuSajang.core.dto.CreateOrderEventDto;
import com.GujjuSajang.core.dto.OrdersProductDto;
import com.GujjuSajang.core.entity.OrdersProduct;
import com.GujjuSajang.core.service.EventProducerService;
import com.GujjuSajang.orders.repository.OrdersProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrdersProductService {

    private final OrdersProductRepository ordersProductRepository;
    private final EventProducerService eventProducerService;

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
    public void updateStatus(List<OrdersProductDto> ordersProductDtoList) {
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

}
