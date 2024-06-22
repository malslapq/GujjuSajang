package com.GujjuSajang.product.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductPageDto {
    private List<ProductDto> products;
    private int pageNumber;
    private int pageSize;
    private long totalCount;
    private int totalPage;
    private boolean last;
}
