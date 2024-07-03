package com.GujjuSajang.core.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class CartProductsDto {

    private Long productId;
    private String name;
    private int count;
    private int price;

}
