package com.GujjuSajang.product.dto;

import com.GujjuSajang.product.entity.Product;
import com.GujjuSajang.product.stock.dto.StockDto;
import com.GujjuSajang.product.stock.entity.Stock;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ProductDetailDto {

    private Long id;
    private Long sellerId;
    private String name;
    private int price;
    private String description;
    private int count;

    public static ProductDetailDto of(Product product, Stock stock) {
        return ProductDetailDto.builder()
                .id(product.getId())
                .sellerId(product.getSellerId()) // Assuming seller is an entity
                .name(product.getName())
                .price(product.getPrice())
                .description(product.getDescription())
                .count(stock.getCount())
                .build();
    }
}
