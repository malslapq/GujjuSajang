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
import com.GujjuSajang.product.stock.service.StockService;
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
import reactor.core.publisher.Sinks;

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
    private final Sinks.Many<StockDto> stockEventSink;
    private final StockService stockService;

    @KafkaListener(topics = {"success-validate-seller-id-from-set-sales-time"}, groupId = "product-service")
    public void setSalesTime(Message<?> message) {

        SetProductSalesStartTimeDto setProductSalesStartTimeDto = objectMapper.convertValue(message.getPayload(), new TypeReference<>() {
        });

        Stock stock = getStocks(setProductSalesStartTimeDto.getProductId());
        stock.changeStartTime(setProductSalesStartTimeDto.getStartTime());

        stockRepository.save(stock);

    }

    @KafkaListener(topics = {"stream-stock"}, groupId = "product-service")
    public void listenStockUpdate(Message<?> message) {
        List<StockDto> stockDtos = objectMapper.convertValue(message.getPayload(), new TypeReference<>() {
        });
        for (StockDto stockDto : stockDtos) {
            stockEventSink.tryEmitNext(stockDto);
        }

    }

    // 재고 수정, 레디스에 반영
    @Transactional
    @KafkaListener(topics = {"stock-update"}, groupId = "product-service")
    public void updateStock(UpdateStockDto updateStockDto) {
        RLock lock = redissonClient.getLock("stock-lock");
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

    // 주문 요청 이벤트 받아서 재고 있는지 확인
    @Transactional
    @KafkaListener(topics = {"create-orders"}, groupId = "product-service")
    public void checkStock(Message<?> message) {
        CreateOrderEventDto createOrderEventDto = null;
        RLock lock = redissonClient.getLock("stock-lock");
        try {
            createOrderEventDto = objectMapper.convertValue(message.getPayload(), new TypeReference<>() {
            });
            List<Long> productIds = createOrderEventDto.getCartProductsDtos().stream().map(CartProductsDto::getProductId).collect(Collectors.toList());

            lock.lock(15, TimeUnit.SECONDS); // 10초 동안 락

            List<StockDto> cachedStocks = stockRedisRepository.getAllByProductIds(productIds);
            Set<Long> cacheHitProductIds = cachedStocks.stream().map(StockDto::getProductId).collect(Collectors.toSet());

            List<Long> cacheMissProductIds = productIds.stream().filter(productId -> !cacheHitProductIds.contains(productId)).toList();
            cachedStocks.addAll(stockRepository.findAllByProductIdIn(cacheMissProductIds).stream().map(StockDto::from).toList());

            Map<Long, Integer> stockCountMap = cachedStocks.stream().collect(Collectors.toMap(StockDto::getProductId, StockDto::getCount));

            checkStocks(createOrderEventDto.getCartProductsDtos(), stockCountMap);

            stockRedisRepository.saveAll(cachedStocks);

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
    @Transactional
    @KafkaListener(topics = {"create-first-come-orders"}, groupId = "product-service")
    public void checkStockAndStartTime(Message<?> message) {
        CreateFirstComeOrdersEventDto createFirstComeOrdersEventDto = null;
        RLock lock = redissonClient.getLock("stock-lock");
        try {
            createFirstComeOrdersEventDto = objectMapper.convertValue(message.getPayload(), new TypeReference<>() {
            });

            lock.lock(10, TimeUnit.SECONDS); // 10초 동안 락
            StockDto stockDto = stockService.getStock(createFirstComeOrdersEventDto.getCartProductsDto().getProductId());
            validateStockAndSalesTime(createFirstComeOrdersEventDto, stockDto.getCount(), stockDto.getStartTime());

            eventProducer.sendEvent("stream-stock", List.of(stockDto));

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

    // 재고 차감
    @Transactional
    @KafkaListener(topics = {"success-create-orders-product"}, groupId = "product-service")
    public void reduceStock(Message<?> message) {
        CreateOrderEventDto createOrderEventDto = null;
        RLock lock = redissonClient.getLock("stock-lock");
        try {
            createOrderEventDto = objectMapper.convertValue(message.getPayload(), new TypeReference<>() {
            });

            List<Long> productIds = createOrderEventDto.getCartProductsDtos().stream().map(CartProductsDto::getProductId).collect(Collectors.toList());

            lock.lock(15, TimeUnit.SECONDS); // 15초 동안 락

            List<StockDto> cachedStocks = stockRedisRepository.getAllByProductIds(productIds);
            Set<Long> cacheHitProductIds = cachedStocks.stream().map(StockDto::getProductId).collect(Collectors.toSet());
            List<Stock> stocks = stockRepository.findAllByProductIdIn(productIds);

            cachedStocks.addAll(stocks.stream().filter(stock -> !cacheHitProductIds.contains(stock.getProductId())).map(StockDto::from).toList());

            Map<Long, Integer> stockCountMap = createOrderEventDto.getCartProductsDtos().stream().collect(Collectors.toMap(CartProductsDto::getProductId, CartProductsDto::getCount));

            for (StockDto stockDto : cachedStocks) {
                int ordersProductCount = stockCountMap.getOrDefault(stockDto.getProductId(), 0);
                if (ordersProductCount > stockDto.getCount()) {
                    throw new OrdersException(ErrorCode.NOT_ENOUGH_STOCK);
                }
                stockDto.setCount(stockDto.getCount() - ordersProductCount);
            }

            for (Stock stock : stocks) {
                int ordersProductCount = stockCountMap.getOrDefault(stock.getProductId(), 0);
                if (ordersProductCount > stock.getCount()) {
                    throw new OrdersException(ErrorCode.NOT_ENOUGH_STOCK);
                }
                stock.updateCount(-ordersProductCount);
            }

            stockRedisRepository.saveAll(cachedStocks);
            stockRepository.saveAll(stocks);

        } catch (Exception e) {
            log.error("error reduceStock stock error orderId : {}", Objects.requireNonNull(createOrderEventDto).getOrderId(), e);
            eventProducer.sendEvent("fail-reduce-stock", createOrderEventDto);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private static void checkStocks(List<CartProductsDto> createOrderEventDto, Map<Long, Integer> stockCountMap) {
        for (CartProductsDto cartProductsDto : createOrderEventDto) {
            int orderProductCount = cartProductsDto.getCount();
            int stockCount = stockCountMap.get(cartProductsDto.getProductId());
            if (orderProductCount > stockCount) {
                throw new OrdersException(ErrorCode.NOT_ENOUGH_STOCK);
            }
        }
    }

    // 주문 제품 상태 반품 완료 이벤트 받아서 재고 늘리기
    @Transactional
    @KafkaListener(topics = {"return-completed-ordersProduct"}, groupId = "product-service")
    public void increaseStockForReturnedProducts(Message<?> message) {
        UpdateOrdersProductStatusDto updateOrdersProductStatusDto = null;
        RLock lock = redissonClient.getLock("stock-lock");
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
