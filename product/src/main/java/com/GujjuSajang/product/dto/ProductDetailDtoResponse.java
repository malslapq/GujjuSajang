package com.GujjuSajang.product.dto;

import com.GujjuSajang.product.entity.Product;
import com.GujjuSajang.product.stock.entity.Stock;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ProductDetailDtoResponse {

    private Long id;
    private String name;
    private int price;
    private String description;
    private int count;

    public static ProductDetailDtoResponse of(Product product, Stock stock) {
        return ProductDetailDtoResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .description(product.getDescription())
                .count(stock.getCount())
                .build();
    }
}
