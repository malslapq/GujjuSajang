package com.GujjuSajang.product.dto;

import com.GujjuSajang.product.entity.Product;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ProductDto {

    private Long id;
    private Long sellerId;
    private String name;
    private int price;
    private String description;

    public static ProductDto from(Product product) {
        return ProductDto.builder()
                .id(product.getId())
                .sellerId(product.getSellerId()) // Assuming seller is an entity
                .name(product.getName())
                .price(product.getPrice())
                .description(product.getDescription())
                .build();
    }
}
