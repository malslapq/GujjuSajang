package com.GujjuSajang.order.dto;

import lombok.*;

import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrdersPageDto {

    private List<OrdersDto> ordersDtos;
    private int pageNumber;
    private int pageSize;
    private long totalCount;
    private int totalPage;
    private boolean last;

}
