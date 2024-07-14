package com.GujjuSajang.webflux.dto;

import com.GujjuSajang.webflux.config.LocalDateTimeDeserializer;
import com.GujjuSajang.webflux.config.LocalDateTimeSerializer;
import com.GujjuSajang.webflux.entity.Stock;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Builder
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class StockDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L; // 추가

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
