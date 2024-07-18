package com.GujjuSajang.product.entity;

import com.GujjuSajang.product.dto.CreateProductDto;
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


    public static Product of(CreateProductDto.Request createProductRequest, Long sellerId) {
        return Product.builder()
                .sellerId(sellerId)
                .name(createProductRequest.getName())
                .price(createProductRequest.getPrice())
                .description(createProductRequest.getDescription())
                .build();
    }

}
