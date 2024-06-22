package com.GujjuSajang.cart.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class UpdateCartProductDto {

    private Long productId;
    private int count;

}
