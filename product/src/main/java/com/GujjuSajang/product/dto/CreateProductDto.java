package com.GujjuSajang.product.dto;

import com.GujjuSajang.product.entity.Product;
import lombok.*;

public class CreateProductDto {

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    @Builder
    public static class Request {

        private String name;
        private int price;
        private String description;

    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    @Builder
    public static class Response {

        private Long id;
        private String name;
        private int price;
        private String description;

        public static CreateProductDto.Response from(Product product) {
            return CreateProductDto.Response.builder()
                    .id(product.getId())
                    .name(product.getName())
                    .price(product.getPrice())
                    .description(product.getDescription())
                    .build();
        }
    }

}
