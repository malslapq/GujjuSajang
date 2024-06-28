package com.GujjuSajang.cart.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CartProductsDto {

    private Long productID;
    private String name;
    private int count;
    private int price;

}
