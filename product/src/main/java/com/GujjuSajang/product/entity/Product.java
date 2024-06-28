package com.GujjuSajang.product.entity;

import com.GujjuSajang.product.dto.ProductDetailDto;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@Table(name = "product", indexes = {
        @Index(name = "idx_product_name", columnList = "name")
})
public class Product {

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
    @CreatedDate
    @Column(updatable = false, nullable = false)
    private LocalDateTime createAt;
    @LastModifiedDate
    @Column
    private LocalDateTime updateAt;

    public static Product from(ProductDetailDto productDetailDto) {
        return Product.builder()
                .sellerId(productDetailDto.getSellerId())
                .name(productDetailDto.getName())
                .price(productDetailDto.getPrice())
                .description(productDetailDto.getDescription())
                .build();
    }

}
