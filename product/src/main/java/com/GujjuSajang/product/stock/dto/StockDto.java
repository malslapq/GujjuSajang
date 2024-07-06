package com.GujjuSajang.product.stock.dto;

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


}
