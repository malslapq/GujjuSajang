package com.GujjuSajang.member.seller.dto;

import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductStockUpdateDto {

    private Long stockId;
    private Long productId;
    private String name;
    private int count;

}
