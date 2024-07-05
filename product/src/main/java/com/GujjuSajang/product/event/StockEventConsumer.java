package com.GujjuSajang.product.event;

import com.GujjuSajang.core.dto.UpdateOrdersProductStatusDto;
import com.GujjuSajang.core.dto.UpdateStockDto;
import com.GujjuSajang.core.exception.ErrorCode;
import com.GujjuSajang.core.exception.ProductException;
import com.GujjuSajang.core.type.OrdersStatus;
import com.GujjuSajang.product.dto.OrdersProductDto;
import com.GujjuSajang.product.entity.Stock;
import com.GujjuSajang.product.repository.StockRepository;
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
public class StockEventConsumer {

    private final StockRepository stockRepository;
    private final EventProducer eventProducer;
    private final ObjectMapper objectMapper;

    // 재고 수정
    @KafkaListener(topics = "stock-update", groupId = "update-stock")
    @Transactional
    public void updateStock(UpdateStockDto updateStockDto) {
        try {
            Stock stock = stockRepository.findByProductId(updateStockDto.getProductId()).orElseThrow(() -> new ProductException(ErrorCode.NOT_FOUND_PRODUCT));
            stock.updateCount(updateStockDto.getCount());
        } catch (Exception e) {
            log.error("update stock error from productId : {}", updateStockDto.getProductId(), e);
        }
    }


    // 주문 생성 이벤트 -> 주문 제품 생성 이벤트 받아서 제품 재고 확인하고 업데이트
    @KafkaListener(topics = {"create-orders-product"}, groupId = "check-stock")
    @Transactional
    public void checkStock(Message<?> message) {
        List<OrdersProductDto> ordersProductDtoList = null;
        try {
            ordersProductDtoList = objectMapper.convertValue(message.getPayload(), new TypeReference<>() {
            });
            List<Long> productIds = ordersProductDtoList.stream().map(OrdersProductDto::getProductId).collect(Collectors.toList());

            List<Stock> stocks = stockRepository.findAllByProductIdIn(productIds);
            Map<Long, Integer> stockCountMap = stocks.stream().collect(Collectors.toMap(Stock::getProductId, Stock::getCount));

            for (OrdersProductDto ordersProductDto : ordersProductDtoList) {
                int orderProductCount = ordersProductDto.getCount();
                int stockCount = stockCountMap.get(ordersProductDto.getProductId());

                if (orderProductCount > stockCount) {
                    ordersProductDto.setStatus(OrdersStatus.INSUFFICIENT_STOCK);
                } else {
                    stockCountMap.put(ordersProductDto.getProductId(), -orderProductCount);
                    ordersProductDto.setStatus(OrdersStatus.COMPLETE);
                }
            }

            // 재고 업데이트
            for (Stock stock : stocks) {
                int remainingStockCount = stockCountMap.getOrDefault(stock.getProductId(), 0);
                stock.updateCount(remainingStockCount);
            }

            stockRepository.saveAll(stocks);

            eventProducer.sendEventWithKey("create-orders-checkStock", "orders", ordersProductDtoList);
        } catch (Exception e) {
            List<Long> productIds = Objects.requireNonNull(ordersProductDtoList).stream().map(OrdersProductDto::getProductId).toList();
            eventProducer.sendEvent("fail-check-stock", productIds);
        }
    }

    // 주문 제품 상태 반품 완료 이벤트 받아서 재고 늘리기
    @KafkaListener(topics = {"return-completed-ordersProduct"}, groupId = "increase-stock-return")
    @Transactional
    public void increaseStockForReturnedProducts(Message<?> message) {
        UpdateOrdersProductStatusDto updateOrdersProductStatusDto = null;
        try {
            updateOrdersProductStatusDto = objectMapper.convertValue(message.getPayload(), new TypeReference<>() {
            });
            Map<Long, Integer> ordersProductCountsMap = updateOrdersProductStatusDto.getOrdersProductCountsMap();
            List<Stock> stocks = stockRepository.findAllByProductIdIn(updateOrdersProductStatusDto.getProductIds());
            for (Stock stock : stocks) {
                stock.updateCount(ordersProductCountsMap.get(stock.getProductId()));
            }
        } catch (Exception e) {
            eventProducer.sendEvent("fail-return-completed-ordersProduct", Objects.requireNonNull(updateOrdersProductStatusDto).getOrdersProductIds());
        }
    }

}
