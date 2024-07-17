package com.GujjuSajang.webflux.service;

import com.GujjuSajang.webflux.dto.StockDto;
import com.GujjuSajang.webflux.exception.ErrorCode;
import com.GujjuSajang.webflux.exception.ProductException;
import com.GujjuSajang.webflux.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StreamService {

    private final StockRepository stockRepository;
    private final CacheManager cacheManager;

    @Cacheable(value = "stock", key = "#productId")
    public Mono<StockDto> getStock(Long productId) {
        return Mono.defer(() -> {
            Cache cache = cacheManager.getCache("stock");
            return Optional.ofNullable(cache)
                    .map(getCache -> getCache.get(productId, StockDto.class))
                    .map(Mono::just)
                    .orElseGet(() -> fetchAndCacheStock(productId, cache));
        });
    }

    private Mono<StockDto> fetchAndCacheStock(Long productId, Cache cache) {
        return stockRepository.findByProductId(productId)
                .map(StockDto::from)
                .doOnNext(dto -> {
                    if (cache != null) {
                        cache.put(productId, dto);
                    }
                })
                .switchIfEmpty(Mono.error(new ProductException(ErrorCode.NOT_FOUND_STOCK)));
    }
}
