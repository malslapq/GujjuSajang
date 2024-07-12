package com.GujjuSajang.product.stock.service;

import com.GujjuSajang.core.exception.ErrorCode;
import com.GujjuSajang.core.exception.ProductException;
import com.GujjuSajang.product.stock.dto.StockDto;
import com.GujjuSajang.product.stock.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class StockService {

    private final StockRepository stockRepository;
    private final CacheManager cacheManager;

    @Cacheable(value = "stock", key = "#productId")
    public StockDto getStock(Long productId) {
        return StockDto.from(stockRepository.findByProductId(productId).orElseThrow(() -> new ProductException(ErrorCode.NOT_FOUND_STOCK)));
    }

    public Mono<StockDto> getStockStream(Long productId) {

        Cache cache = cacheManager.getCache("stock");
        StockDto stockDto = Objects.requireNonNull(cache).get(productId, StockDto.class);

        if (stockDto != null) {
            return Mono.just(stockDto);
        }

        return Mono.justOrEmpty(stockRepository.findByProductId(productId))
                .map(StockDto::from)
                .doOnNext(dto -> cache.put(productId, dto))
                .switchIfEmpty(Mono.error(new ProductException(ErrorCode.NOT_FOUND_STOCK)));
    }

}
