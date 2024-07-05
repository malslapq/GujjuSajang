package com.GujjuSajang.product.repository;

import com.GujjuSajang.product.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {
    Optional<Stock> findByProductId(Long id);

    List<Stock> findAllByProductIdIn(List<Long> productIds);

}
