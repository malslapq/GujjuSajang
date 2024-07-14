package com.GujjuSajang.webflux.repository;

import com.GujjuSajang.webflux.entity.Stock;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface StockRepository extends ReactiveCrudRepository<Stock, Long> {
    Mono<Stock> findByProductId(Long productId);
}
