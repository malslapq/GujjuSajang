package com.GujjuSajang.core.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CartDto {

    private Long ordersId;
    private List<CartProductsDto> cartProductsDtos = new ArrayList<>();

}
