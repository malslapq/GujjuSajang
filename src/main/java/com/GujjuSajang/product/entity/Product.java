package com.GujjuSajang.product.entity;

import com.GujjuSajang.product.dto.ProductDto;
import com.GujjuSajang.util.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@Table(name = "product", indexes = {
        @Index(name = "idx_product_name", columnList = "name")
})
public class Product extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Long sellerId;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private int price;
    @Column(nullable = false)
    private String description;

    public static ProductDto from(ProductDto productDto) {
        return ProductDto.builder()
                .id(productDto.getId())
                .sellerId(productDto.getSellerId())
                .name(productDto.getName())
                .price(productDto.getPrice())
                .description(productDto.getDescription())
                .build();
    }

}
