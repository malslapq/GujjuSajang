package com.GujjuSajang.product;

import com.GujjuSajang.core.exception.ErrorCode;
import com.GujjuSajang.core.exception.ProductException;
import com.GujjuSajang.product.stock.dto.StockDto;
import com.GujjuSajang.product.stock.entity.Stock;
import com.GujjuSajang.product.stock.repository.StockRepository;
import com.GujjuSajang.product.stock.service.StockService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.TestPropertySource;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
        "redis.host=localhost",
        "redis.port=6379",
        "redisson.address=redis://localhost:6379",
        "openapi.service.url=http://localhost:8080",
        "eureka.client.enabled=false",
        "spring.kafka.consumer.auto-startup=false",
        "spring.kafka.producer.auto-startup=false"
})
public class StockServiceTest {

    @Autowired
    private StockService stockService;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private StockRepository stockRepository;

    @BeforeEach
    void setUp() {
        Objects.requireNonNull(cacheManager.getCache("stock")).clear();
        stockRepository.deleteAll();
    }

    @DisplayName("재고 조회 성공 - 캐시 적용")
    @Test
    void getStock_Success_Cacheable() {
        // given
        Long productId = 1L;
        Stock stock = Stock.builder()
                .productId(productId)
                .count(100)
                .build();
        stockRepository.save(stock);
        StockDto stockDto = StockDto.from(stock);

        // when
        stockService.getStock(productId);

        // then
        Cache cache = cacheManager.getCache("stock");
        assertNotNull(cache);
        Cache.ValueWrapper cachedValue = cache.get(productId);
        assertNotNull(cachedValue);
        StockDto cachedStockDto = (StockDto) cachedValue.get();
        assertEquals(stockDto.getId(), cachedStockDto.getId());
        assertEquals(stockDto.getProductId(), cachedStockDto.getProductId());
        assertEquals(stockDto.getCount(), cachedStockDto.getCount());
    }


    @DisplayName("재고 조회 성공 - 캐시 미적용")
    @Test
    void getStock_Success_NoCache() {
        // given
        Long productId = 2L;
        Stock stock = Stock.builder()
                .productId(productId)
                .count(200)
                .build();
        stockRepository.save(stock);
        StockDto stockDto = StockDto.from(stock);
        Objects.requireNonNull(cacheManager.getCache("stock")).clear();

        // when
        StockDto result = stockService.getStock(productId);

        // then
        assertEquals(stockDto.getId(), result.getId());
        assertEquals(stockDto.getProductId(), result.getProductId());
        assertEquals(stockDto.getCount(), result.getCount());
    }

    @DisplayName("재고 조회 실패 - 제품의 재고정보 없음")
    @Test
    void getStock_Fail_NotFoundProductStock() {
        // given
        Long productId = 3L;

        // when

        // then
        ProductException exception = assertThrows(ProductException.class, () -> stockService.getStock(productId));
        assertEquals(ErrorCode.NOT_FOUND_STOCK, exception.getErrorCode());
    }
}
