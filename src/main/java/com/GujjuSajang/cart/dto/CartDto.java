package com.GujjuSajang.cart.dto;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class CartDto {

    private List<CartProductsDto> cartProductsDtos = new ArrayList<>();

}
