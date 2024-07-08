package com.GujjuSajang.product.stock.dto;

import com.GujjuSajang.product.stock.entity.Stock;
import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StockDto {

    private Long id;
    private Long productId;
    private int count;

    public static StockDto from(Stock stock) {
        return StockDto.builder()
                .id(stock.getId())
                .productId(stock.getProductId())
                .count(stock.getCount())
                .build();
    }

}
