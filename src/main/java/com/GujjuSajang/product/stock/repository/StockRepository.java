package com.GujjuSajang.product.stock.repository;

import com.GujjuSajang.product.stock.Entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {
    Optional<Stock> findByProductId(Long id);

    List<Stock> findAllByProductIdIn(List<Long> productIds);

}