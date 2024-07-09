package com.GujjuSajang.product.stock.service;

import com.GujjuSajang.core.exception.ErrorCode;
import com.GujjuSajang.core.exception.ProductException;
import com.GujjuSajang.product.stock.dto.StockDto;
import com.GujjuSajang.product.stock.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StockService {

    private final StockRepository stockRepository;

    @Cacheable(value = "stock", key = "#productId")
    public StockDto getStock(Long productId) {
        return StockDto.from(stockRepository.findByProductId(productId).orElseThrow(() -> new ProductException(ErrorCode.NOT_FOUND_STOCK)));
    }

}
