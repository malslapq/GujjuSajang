package com.GujjuSajang.service;


import com.GujjuSajang.exception.ErrorCode;
import com.GujjuSajang.exception.ProductException;
import com.GujjuSajang.product.stock.Entity.Stock;
import com.GujjuSajang.product.stock.dto.StockDto;
import com.GujjuSajang.product.stock.repository.StockRepository;
import com.GujjuSajang.product.stock.service.StockService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StockServiceTest {

    @Mock
    private StockRepository stockRepository;

    @InjectMocks
    private StockService stockService;

    private StockDto stockDto;
    private Stock stock;

    @BeforeEach
    void setUp() {
        stockDto = StockDto.builder()
                .productId(1L)
                .count(10)
                .build();

        stock = Stock.builder()
                .id(1L)
                .productId(1L)
                .count(5)
                .build();
    }

    @DisplayName("재고 업데이트 성공")
    @Test
    void updateStock_Success() {
        // given
        when(stockRepository.findByProductId(anyLong())).thenReturn(Optional.of(stock));

        // when
        stockService.updateStock(stockDto);

        // then
        verify(stockRepository, times(1)).findByProductId(anyLong());
        assertThat(stock.getCount()).isEqualTo(15); // 5 + 10 = 15
    }

    @DisplayName("재고 업데이트 실패 - 제품 없음")
    @Test
    void updateStock_Fail_ProductNotFound() {
        // given
        when(stockRepository.findByProductId(anyLong())).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> stockService.updateStock(stockDto))
                .isInstanceOf(ProductException.class)
                .hasMessageContaining(ErrorCode.NOT_FOUND_PRODUCT.getErrorMessage());
    }
}