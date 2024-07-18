package com.GujjuSajang.product.stock.event;

import com.GujjuSajang.core.dto.*;
import com.GujjuSajang.core.exception.ErrorCode;
import com.GujjuSajang.core.exception.ProductException;
import com.GujjuSajang.product.event.EventProducer;
import com.GujjuSajang.product.stock.dto.StockDto;
import com.GujjuSajang.product.stock.entity.Stock;
import com.GujjuSajang.product.stock.repository.StockCustomRepository;
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

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockEventConsumer {

    private final StockRepository stockRepository;
    private final StockCustomRepository stockCustomRepository;
    private final EventProducer eventProducer;
    private final ObjectMapper objectMapper;
    private final StockRedisRepository stockRedisRepository;
    private final RedissonClient redissonClient;
    private final StockService stockService;

    @KafkaListener(topics = {"success-validate-seller-id-from-set-sales-time"})
    public void setSalesTime(Message<?> message) {

        SetProductSalesStartTimeDto setProductSalesStartTimeDto = objectMapper.convertValue(message.getPayload(), new TypeReference<>() {
        });

        Stock stock = getStocks(setProductSalesStartTimeDto.getProductId());
        stock.changeStartTime(setProductSalesStartTimeDto.getStartTime());

        stockRepository.save(stock);

    }

    // 재고 수정, 레디스에 반영
    @Transactional
    @KafkaListener(topics = {"stock-update"})
    public void updateStock(UpdateStockDto updateStockDto) {
        RLock lock = redissonClient.getLock("stock-lock");
        try {
            lock.lock(5, TimeUnit.SECONDS);
            Stock stock = stockRepository.findByProductId(updateStockDto.getProductId()).orElseThrow(() -> new ProductException(ErrorCode.NOT_FOUND_PRODUCT));
            stock.deductCount(updateStockDto.getCount());
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

    // 주문 요청 이벤트 받아서 redis 재고 확인 및 차감
    @Transactional
    @KafkaListener(topics = {"request-orders"})
    public void checkStock(Message<?> message) {
        CreateOrderEventDto createOrderEventDto = null;
        RLock lock = redissonClient.getLock("stock-lock");
        try {
            createOrderEventDto = objectMapper.convertValue(message.getPayload(), new TypeReference<>() {
            });
            List<Long> productIds = createOrderEventDto.getCartProductsDtos().stream().map(CartProductsDto::getProductId).toList();

            lock.lock(15, TimeUnit.SECONDS);

            // 캐시된 재고 가져오기
            List<StockDto> cachedStocks = stockRedisRepository.getAllByProductIds(productIds);

            // 제품들 가져와서 캐시 안된 제품들 dto로 만들어서 캐싱리스트에 넣기
            Set<Long> cacheHitProductIds = cachedStocks.stream().map(StockDto::getProductId).collect(Collectors.toSet());
            List<Long> cacheMissProductIds = productIds.stream().filter(productId -> !cacheHitProductIds.contains(productId)).toList();
            List<Stock> stocks = stockRepository.findAllByProductIdIn(cacheMissProductIds);
            cachedStocks.addAll(stocks.stream().map(StockDto::from).toList());

            // 주문한 수량 map 생성
            Map<Long, Integer> ordersProductCountMap = createOrderEventDto.getCartProductsDtos().stream().collect(Collectors.toMap(CartProductsDto::getProductId, CartProductsDto::getCount));

            // 재고 확인
            for (StockDto cachedStock : cachedStocks) {
                if (cachedStock.getCount() < ordersProductCountMap.get(cachedStock.getProductId())) {
                    throw new ProductException(ErrorCode.NOT_ENOUGH_STOCK);
                }
            }

            // 재고 차감
            for (StockDto stockDto : cachedStocks) {
                stockDto.setCount(stockDto.getCount() - ordersProductCountMap.getOrDefault(stockDto.getProductId(), 0));
            }

            // 캐싱
            stockRedisRepository.saveAll(cachedStocks);

            eventProducer.sendEvent("succeed-check-stock", createOrderEventDto);

        } catch (Exception e) {
            log.error("check stock error message : {}", createOrderEventDto, e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    // db 반영
    @KafkaListener(topics = {"created-orders-product"})
    @Transactional
    public void deductStock(Message<?> message) {
        CreateOrderEventDto createOrderEventDto = null;
        RLock lock = redissonClient.getLock("stock-lock");
        try {
            createOrderEventDto = objectMapper.convertValue(message.getPayload(), new TypeReference<>() {
            });
            Map<Long, Integer> ordersProductCountMap = createOrderEventDto.getCartProductsDtos().stream().collect(Collectors.toMap(CartProductsDto::getProductId, CartProductsDto::getCount));
            lock.lock(15, TimeUnit.SECONDS);

            stockCustomRepository.batchDecrementStockCounts(ordersProductCountMap);

        } catch (Exception e) {
            log.error("deductStock error message : {}", createOrderEventDto, e);
            eventProducer.sendEvent("failed-deduct-stock", createOrderEventDto);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    // 선착순 구매 재고 확인
    @Transactional
    @KafkaListener(topics = {"create-first-come-orders"})
    public void checkStockAndStartTime(Message<?> message) {
        CreateFirstComeOrdersEventDto createFirstComeOrdersEventDto = null;
        RLock lock = redissonClient.getLock("first-come-stock-lock");
        try {
            createFirstComeOrdersEventDto = objectMapper.convertValue(message.getPayload(), new TypeReference<>() {
            });

            lock.lock(15, TimeUnit.SECONDS);
            StockDto stockDto = stockService.getStock(createFirstComeOrdersEventDto.getCartProductsDto().getProductId());
            validateStockAndSalesTime(createFirstComeOrdersEventDto, stockDto.getCount(), stockDto.getStartTime());

            stockDto.setCount(stockDto.getCount() - createFirstComeOrdersEventDto.getCartProductsDto().getCount());

            stockRedisRepository.save(stockDto);

            eventProducer.sendEvent("stream-stock", List.of(stockDto));

            eventProducer.sendEvent("succeed-check-stock", CreateOrderEventDto.builder()
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

    // 재고 복구
    @Transactional
    @KafkaListener(topics = {"cancel-payment", "failed-payment", "failed-request-payment"})
    public void resetStock(Message<?> message) {
        CreateOrderEventDto createOrderEventDto = null;
        RLock lock = redissonClient.getLock("stock-lock");
        try {

            createOrderEventDto = objectMapper.convertValue(message.getPayload(), new TypeReference<>() {
            });
            List<Long> productIds = createOrderEventDto.getCartProductsDtos().stream().map(CartProductsDto::getProductId).toList();

            lock.lock(15, TimeUnit.SECONDS);
            List<StockDto> cachedStocks = stockRedisRepository.getAllByProductIds(productIds);
            Map<Long, Integer> ordersProductCountMap = createOrderEventDto.getCartProductsDtos().stream().collect(Collectors.toMap(CartProductsDto::getProductId, CartProductsDto::getCount));

            for (StockDto stockDto : cachedStocks) {
                stockDto.setCount(stockDto.getCount() + ordersProductCountMap.getOrDefault(stockDto.getProductId(), 0));
            }

            stockRedisRepository.saveAll(cachedStocks);

        } catch (Exception e) {
            log.error("failed reset stock error message : {}", createOrderEventDto, e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    // 주문 제품 상태 반품 완료 이벤트 받아서 재고 늘리기
    @Transactional
    @KafkaListener(topics = {"return-completed-ordersProduct"})
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
                stock.deductCount(ordersProductCountsMap.get(stock.getProductId()));
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
