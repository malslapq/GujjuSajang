package com.GujjuSajang.product.stock.entity;

import com.GujjuSajang.product.entity.BaseTimeEntity;
import com.GujjuSajang.product.stock.dto.StockDto;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Stock extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long productId;
    private int count;
    private LocalDateTime startTime;

    public void updateCount(int count) {
        this.count = this.count - count;
    }

    public static Stock from(StockDto stockDto) {
        return Stock.builder()
                .id(stockDto.getId())
                .productId(stockDto.getProductId())
                .count(stockDto.getCount())
                .build();
    }

    public void changeStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

}
