package com.GujjuSajang.product.stock.event;

import com.GujjuSajang.core.dto.CartProductsDto;
import com.GujjuSajang.core.dto.CreateOrderEventDto;
import com.GujjuSajang.core.dto.UpdateOrdersProductStatusDto;
import com.GujjuSajang.core.dto.UpdateStockDto;
import com.GujjuSajang.core.exception.ErrorCode;
import com.GujjuSajang.core.exception.OrdersException;
import com.GujjuSajang.core.exception.ProductException;
import com.GujjuSajang.product.event.EventProducer;
import com.GujjuSajang.product.stock.entity.Stock;
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
    @KafkaListener(topics = "stock-update", groupId = "stock-service")
    @Transactional
    public void updateStock(UpdateStockDto updateStockDto) {
        try {
            Stock stock = stockRepository.findByProductId(updateStockDto.getProductId()).orElseThrow(() -> new ProductException(ErrorCode.NOT_FOUND_PRODUCT));
            stock.updateCount(updateStockDto.getCount());
        } catch (Exception e) {
            log.error("update stock error from productId : {}", updateStockDto.getProductId(), e);
        }
    }

    // 주문 생성 이벤트 받아서 재고 있는지 먼저 확인
    @KafkaListener(topics = {"create-orders"}, groupId = "stock-service")
    @Transactional
    public void checkStock(Message<?> message) {
        CreateOrderEventDto createOrderEventDto = null;
        try {
            createOrderEventDto = objectMapper.convertValue(message.getPayload(), new TypeReference<>() {
            });
            List<Long> productIds = createOrderEventDto.getCartProductsDtos().stream().map(CartProductsDto::getProductId).collect(Collectors.toList());

            List<Stock> stocks = stockRepository.findAllByProductIdIn(productIds);
            Map<Long, Integer> stockCountMap = stocks.stream().collect(Collectors.toMap(Stock::getProductId, Stock::getCount));

            for (CartProductsDto cartProductsDto : createOrderEventDto.getCartProductsDtos()) {
                int orderProductCount = cartProductsDto.getCount();
                log.error("order count : {}", orderProductCount);
                int stockCount = stockCountMap.get(cartProductsDto.getProductId());

                if (orderProductCount <= stockCount) {
                    stockCountMap.put(cartProductsDto.getProductId(), -orderProductCount);
                    log.error("update stock count : {}", stockCount);
                } else {
                    throw new OrdersException(ErrorCode.NOT_ENOUGH_STOCK);
                }
            }

            // 재고 업데이트
            for (Stock stock : stocks) {
                int remainingStockCount = stockCountMap.getOrDefault(stock.getProductId(), 0);
                stock.updateCount(remainingStockCount);
            }

            eventProducer.sendEvent("success-check-stock", createOrderEventDto);
        } catch (Exception e) {
            eventProducer.sendEvent("fail-check-stock", Objects.requireNonNull(createOrderEventDto).getOrderId());
        }
    }

    // 주문 제품들 생성 실패 이벤트 받아서 차감한 재고 복원
    @Transactional
    @KafkaListener(topics = {"fail-create-orders-product"}, groupId = "stock-service")
    public void resetStock(Message<?> message) {
        CreateOrderEventDto createOrderEventDto = null;
        try {
            createOrderEventDto = objectMapper.convertValue(message.getPayload(), new TypeReference<>() {
            });

            List<Stock> stocks = getStocks(createOrderEventDto);

            Map<Long, Integer> stockCountMap = stocks.stream().collect(Collectors.toMap(Stock::getProductId, Stock::getCount));

            for (CartProductsDto cartProductsDto : createOrderEventDto.getCartProductsDtos()) {
                int orderProductCount = cartProductsDto.getCount();
                stockCountMap.put(cartProductsDto.getProductId(), orderProductCount);
            }

            // 재고 업데이트
            for (Stock stock : stocks) {
                int resetStockCount = stockCountMap.getOrDefault(stock.getProductId(), 0);
                stock.updateCount(resetStockCount);
            }

            eventProducer.sendEvent("reset-stock-count", createOrderEventDto.getOrderId());

        } catch (Exception e) {
            log.error("reset stock error from message : {}", message, e);
        }
    }


    // 주문 제품 상태 반품 완료 이벤트 받아서 재고 늘리기
    @KafkaListener(topics = {"return-completed-ordersProduct"}, groupId = "stock-service")
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

    private List<Stock> getStocks(CreateOrderEventDto createOrderEventDto) {
        List<Long> productIds = createOrderEventDto.getCartProductsDtos().stream().map(CartProductsDto::getProductId).collect(Collectors.toList());
        return stockRepository.findAllByProductIdIn(productIds);
    }
}
