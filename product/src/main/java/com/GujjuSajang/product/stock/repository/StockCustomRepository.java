package com.GujjuSajang.product.stock.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class StockCustomRepository {

    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public void batchDecrementStockCounts(Map<Long, Integer> productCountMap) {
        String sql = "UPDATE stock SET count = count - ? WHERE product_id = ?";

        List<Object[]> batchArgs = productCountMap.entrySet().stream()
                .map(entry -> new Object[]{entry.getValue(), entry.getKey()})
                .collect(Collectors.toList());

        jdbcTemplate.batchUpdate(sql, batchArgs);
    }

}
