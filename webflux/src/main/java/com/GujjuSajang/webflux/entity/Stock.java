package com.GujjuSajang.webflux.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "stock")
public class Stock {

    @Id
    private Long id;
    private Long productId;
    private int count;
    private LocalDateTime startTime;
    private LocalDateTime createdAt;

}
