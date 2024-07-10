package com.GujjuSajang.product.stock.event;

import com.GujjuSajang.core.dto.*;
import com.GujjuSajang.core.exception.ErrorCode;
import com.GujjuSajang.core.exception.OrdersException;
import com.GujjuSajang.core.exception.ProductException;
import com.GujjuSajang.product.event.EventProducer;
import com.GujjuSajang.product.stock.dto.StockDto;
import com.GujjuSajang.product.stock.entity.Stock;
import com.GujjuSajang.product.stock.repository.StockRedisRepository;
import com.GujjuSajang.product.stock.repository.StockRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockEventConsumer {

    private final StockRepository stockRepository;
    private final EventProducer eventProducer;
    private final ObjectMapper objectMapper;
    private final StockRedisRepository stockRedisRepository;
    private final RedissonClient redissonClient;

    // 재고 수정, 레디스에 반영
    @KafkaListener(topics = "stock-update", groupId = "product-service")
    @Transactional
    public void updateStock(UpdateStockDto updateStockDto) {
        RLock lock = redissonClient.getLock("stock-check-lock");
        try {
            lock.lock(5, TimeUnit.SECONDS);
            Stock stock = stockRepository.findByProductId(updateStockDto.getProductId()).orElseThrow(() -> new ProductException(ErrorCode.NOT_FOUND_PRODUCT));
            stock.updateCount(updateStockDto.getCount());
            stockRepository.save(stock);
            stockRedisRepository.save(StockDto.from(stock));
        } catch (Exception e) {
            log.error("update stock error from productId : {}", updateStockDto.getProductId(), e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    // 주문 요청 이벤트 받아서 재고 있는지 확인 , 레디스에 있을 경우, 없을 경우 나눔
    @KafkaListener(topics = {"create-orders"}, groupId = "product-service")
    @Transactional
    public void checkStock(Message<?> message) {
        CreateOrderEventDto createOrderEventDto = null;
        RLock lock = redissonClient.getLock("stock-check-lock");
        try {
            createOrderEventDto = objectMapper.convertValue(message.getPayload(), new TypeReference<>() {
            });
            List<Long> productIds = createOrderEventDto.getCartProductsDtos().stream().map(CartProductsDto::getProductId).collect(Collectors.toList());

            lock.lock(10, TimeUnit.SECONDS); // 10초 동안 락

            List<StockDto> stockDtos = stockRedisRepository.getAllByProductIds(productIds);

            if (stockDtos.isEmpty()) {
                checkStockFromDB(createOrderEventDto.getCartProductsDtos(), productIds);
            } else {
                checkStockFromRedis(createOrderEventDto.getCartProductsDtos(), productIds, stockDtos);
            }

            eventProducer.sendEvent("success-check-stock", createOrderEventDto);
        } catch (Exception e) {
            log.error("check stock error message : {}", createOrderEventDto, e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    // 선착순 구매 재고 확인
    @KafkaListener(topics = {"create-first-come-orders"}, groupId = "product-service")
    public void checkStockAndStartTime(Message<?> message) {
        CreateFirstComeOrdersEventDto createFirstComeOrdersEventDto = null;
        RLock lock = redissonClient.getLock("stock-check-lock-from-first-come-orders");
        try {
            createFirstComeOrdersEventDto = objectMapper.convertValue(message.getPayload(), new TypeReference<>() {
            });

            lock.lock(10, TimeUnit.SECONDS); // 10초 동안 락
            Optional<StockDto> stockDto = stockRedisRepository.get(createFirstComeOrdersEventDto.getCartProductsDto().getProductId());

            if (stockDto.isEmpty()) {
                Stock stock = getStocks(createFirstComeOrdersEventDto.getCartProductsDto().getProductId());

                validateStockAndSalesTime(createFirstComeOrdersEventDto, stock.getCount(), stock.getStartTime());

                stock.updateCount(-createFirstComeOrdersEventDto.getCartProductsDto().getCount());
                stockRedisRepository.save(StockDto.from(stock));
                stockRepository.save(stock);
            } else {

                validateStockAndSalesTime(createFirstComeOrdersEventDto, stockDto.get().getCount(), stockDto.get().getStartTime());

                stockDto.get().setCount(stockDto.get().getCount() - createFirstComeOrdersEventDto.getCartProductsDto().getCount());

                stockRepository.setStockCount(createFirstComeOrdersEventDto.getCartProductsDto().getProductId(), stockDto.get().getCount());
                stockRedisRepository.save(stockDto.get());
            }

            eventProducer.sendEvent("success-check-stock", CreateOrderEventDto.builder()
                    .memberId(createFirstComeOrdersEventDto.getMemberId())
                    .cartProductsDtos(new ArrayList<>(Collections.singletonList(createFirstComeOrdersEventDto.getCartProductsDto())))
                    .build());

        } catch (Exception e) {
            log.error("first come orders check stock error message : {}", createFirstComeOrdersEventDto, e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    // 주문 , 결제 실패 ,주문 제품 생성 실패 이벤트 받아서 차감한 재고 복원
    @Transactional
    @KafkaListener(topics = {"fail-create-orders", "fail-payment", "fail-create-orders-product"}, groupId = "product-service")
    public void resetStock(Message<?> message) {
        CreateOrderEventDto createOrderEventDto = null;
        RLock lock = redissonClient.getLock("reset-stock-lock");
        try {
            createOrderEventDto = objectMapper.convertValue(message.getPayload(), new TypeReference<>() {
            });
            List<Long> productIds = createOrderEventDto.getCartProductsDtos().stream().map(CartProductsDto::getProductId).toList();

            lock.lock(10, TimeUnit.SECONDS);

            List<Stock> stocks = getAllStocksFromProductIds(productIds);
            // 재고 복원할 제품 찾기
            for (CartProductsDto cartProductsDto : createOrderEventDto.getCartProductsDtos()) {
                Stock stock = stocks.stream()
                        .filter(s -> s.getProductId().equals(cartProductsDto.getProductId()))
                        .findFirst()
                        .orElseThrow(() -> new ProductException(ErrorCode.NOT_FOUND_PRODUCT));
                // 복원
                stock.updateCount(cartProductsDto.getCount());
            }

            // db에 저장하고 변경 내용 Redis에 저장
            List<StockDto> stockDtos = stocks.stream().map(StockDto::from).collect(Collectors.toList());
            stockRedisRepository.saveAll(stockDtos);
            stockRepository.saveAll(stocks);

            eventProducer.sendEvent("reset-stock-count", createOrderEventDto.getOrderId());

        } catch (Exception e) {
            log.error("reset stock error from message : {}", message, e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    // 주문 제품 상태 반품 완료 이벤트 받아서 재고 늘리기
    @KafkaListener(topics = {"return-completed-ordersProduct"}, groupId = "product-service")
    @Transactional
    public void increaseStockForReturnedProducts(Message<?> message) {
        UpdateOrdersProductStatusDto updateOrdersProductStatusDto = null;
        RLock lock = redissonClient.getLock("return-completed-stock-lock");
        try {
            updateOrdersProductStatusDto = objectMapper.convertValue(message.getPayload(), new TypeReference<>() {
            });
            Map<Long, Integer> ordersProductCountsMap = updateOrdersProductStatusDto.getOrdersProductCountsMap();

            lock.lock(10, TimeUnit.SECONDS);

            List<Stock> stocks = getAllStocksFromProductIds(updateOrdersProductStatusDto.getProductIds());
            for (Stock stock : stocks) {
                stock.updateCount(ordersProductCountsMap.get(stock.getProductId()));
            }
            List<StockDto> stockDtos = stocks.stream().map(StockDto::from).toList();

            stockRepository.saveAll(stocks);
            stockRedisRepository.saveAll(stockDtos);

        } catch (Exception e) {
            eventProducer.sendEvent("fail-return-completed-ordersProduct", Objects.requireNonNull(updateOrdersProductStatusDto).getOrdersProductIds());
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private void checkStockFromRedis(List<CartProductsDto> cartProductsDtos, List<Long> productIds, List<StockDto> stockDtos) {
        Map<Long, Integer> stockCountMap = stockDtos.stream().collect(Collectors.toMap(StockDto::getProductId, StockDto::getCount));
        List<Stock> stocks = getAllStocksFromProductIds(productIds);
        deductStockForOrders(cartProductsDtos, stockCountMap);

        for (StockDto stockDto : stockDtos) {
            int remainingStockCount = stockCountMap.getOrDefault(stockDto.getProductId(), 0);
            stockDto.setCount(stockDto.getCount() + remainingStockCount);
        }

        for (Stock stock : stocks) {
            int remainingStockCount = stockCountMap.getOrDefault(stock.getProductId(), 0);
            stock.updateCount(remainingStockCount);
        }

        stockRedisRepository.saveAll(stockDtos);
        stockRepository.saveAll(stocks);

    }

    private void checkStockFromDB(List<CartProductsDto> cartProductsDtos, List<Long> productIds) {
        List<Stock> stocks = getAllStocksFromProductIds(productIds);
        Map<Long, Integer> stockCountMap = stocks.stream().collect(Collectors.toMap(Stock::getProductId, Stock::getCount));
        deductStockForOrders(cartProductsDtos, stockCountMap);

        for (Stock stock : stocks) {
            int remainingStockCount = stockCountMap.getOrDefault(stock.getProductId(), 0);
            stock.updateCount(remainingStockCount);
        }

        List<StockDto> stockDtos = stocks.stream().map(StockDto::from).toList();

        stockRedisRepository.saveAll(stockDtos);
        stockRepository.saveAll(stocks);
    }


    private void deductStockForOrders(List<CartProductsDto> cartProductsDtos, Map<Long, Integer> stockCountMap) {
        for (CartProductsDto cartProductsDto : cartProductsDtos) {
            int orderProductCount = cartProductsDto.getCount();
            int stockCount = stockCountMap.get(cartProductsDto.getProductId());

            if (orderProductCount <= stockCount) {
                stockCountMap.put(cartProductsDto.getProductId(), -orderProductCount);
            } else {
                throw new OrdersException(ErrorCode.NOT_ENOUGH_STOCK);
            }
        }
    }

    private void validateStockAndSalesTime(CreateFirstComeOrdersEventDto createFirstComeOrdersEventDto, int stockCount, LocalDateTime startTime) {

        if (stockCount < createFirstComeOrdersEventDto.getCartProductsDto().getCount()) {
            throw new ProductException(ErrorCode.NOT_ENOUGH_STOCK);
        }
        if (LocalDateTime.now().isBefore(startTime)) {
            throw new ProductException(ErrorCode.NOT_SALES_TIME);
        }
    }

    private List<Stock> getAllStocksFromProductIds(List<Long> productIds) {
        return stockRepository.findAllByProductIdIn(productIds);
    }

    private Stock getStocks(Long productId) {
        return stockRepository.findByProductId(productId).orElseThrow(() -> new ProductException(ErrorCode.NOT_FOUND_PRODUCT));
    }
}
