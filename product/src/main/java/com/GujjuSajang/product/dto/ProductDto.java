package com.GujjuSajang.product.dto;

import com.GujjuSajang.product.entity.Product;
import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductDto {

    private Long id;
    private String name;
    private int price;

    public static ProductDto from(Product product) {
        return ProductDto.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .build();
    }

}
