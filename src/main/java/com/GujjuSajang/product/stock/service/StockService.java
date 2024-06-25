package com.GujjuSajang.product.stock.service;

import com.GujjuSajang.exception.ErrorCode;
import com.GujjuSajang.exception.ProductException;
import com.GujjuSajang.product.stock.Entity.Stock;
import com.GujjuSajang.product.stock.dto.StockDto;
import com.GujjuSajang.product.stock.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StockService {

    private final StockRepository stockRepository;

    @Transactional
    public void updateStock(StockDto stockDto) {
        Stock stock = stockRepository.findByProductId(stockDto.getProductId()).orElseThrow(() -> new ProductException(ErrorCode.NOT_FOUND_PRODUCT));
        stock.updateCount(stockDto.getCount());
    }

}
