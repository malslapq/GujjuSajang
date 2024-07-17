package com.GujjuSajang.product.stock.repository;

import com.GujjuSajang.product.stock.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {
    Optional<Stock> findByProductId(Long id);

    List<Stock> findAllByProductIdIn(List<Long> productIds);

    @Modifying
    @Transactional
    @Query(value = "UPDATE stock s SET s.count = s.count - :count WHERE s.product_id = :productId", nativeQuery = true)
    void decrementStockCount(@Param("productId") Long productId, @Param("count") int count);

}
