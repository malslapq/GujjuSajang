package com.GujjuSajang.product.stock.service;

import com.GujjuSajang.core.dto.OrdersProductDto;
import com.GujjuSajang.core.dto.UpdateOrdersProductStatusDto;
import com.GujjuSajang.core.dto.UpdateStockDto;
import com.GujjuSajang.core.exception.ErrorCode;
import com.GujjuSajang.core.exception.ProductException;
import com.GujjuSajang.core.service.EventProducerService;
import com.GujjuSajang.core.type.OrdersStatus;
import com.GujjuSajang.product.stock.Entity.Stock;
import com.GujjuSajang.product.stock.repository.StockRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StockService {

    private final StockRepository stockRepository;
    private final EventProducerService eventProducerService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "stock-update", groupId = "stock-update")
    @Transactional
    public void updateStock(UpdateStockDto updateStockDto) {
        Stock stock = stockRepository.findByProductId(updateStockDto.getProductId()).orElseThrow(() -> new ProductException(ErrorCode.NOT_FOUND_PRODUCT));
        stock.updateCount(updateStockDto.getCount());
    }

    @KafkaListener(topics = {"create-orders-product"}, groupId = "stock-check")
    @Transactional
    public void checkStock(Message<?> message) {

        List<OrdersProductDto> ordersProductDtoList = objectMapper.convertValue(message.getPayload(), new TypeReference<>() {
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

        eventProducerService.sendEventWithKey("checkStock", "orders", ordersProductDtoList);
    }

    @KafkaListener(topics = {"update-ordersProduct-status"}, groupId = "increase-stock-return")
    @Transactional
    public void increaseStockForReturnedProducts(Message<?> message) {

        UpdateOrdersProductStatusDto updateOrdersProductStatusDto = objectMapper.convertValue(message.getPayload(), new TypeReference<>() {});
        Map<Long, Integer> ordersProductCountsMap = updateOrdersProductStatusDto.getOrdersProductCountsMap();
        List<Stock> stocks = stockRepository.findAllByProductIdIn(updateOrdersProductStatusDto.getProductIds());
        for (Stock stock : stocks) {
            stock.updateCount(ordersProductCountsMap.get(stock.getProductId()));
        }

    }

}
