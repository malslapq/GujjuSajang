package com.GujjuSajang.product.stock.dto;

import com.GujjuSajang.product.config.LocalDateTimeDeserializer;
import com.GujjuSajang.product.config.LocalDateTimeSerializer;
import com.GujjuSajang.product.stock.entity.Stock;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.*;

import java.time.LocalDateTime;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StockDto {

    private Long id;
    private Long productId;
    private int count;
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime startTime;

    public static StockDto from(Stock stock) {
        return StockDto.builder()
                .id(stock.getId())
                .productId(stock.getProductId())
                .count(stock.getCount())
                .startTime(stock.getStartTime())
                .build();
    }

}
