package com.GujjuSajang.core.dto;

import lombok.*;

import java.util.List;
import java.util.Map;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateOrdersProductStatusDto {

    private List<Long> productIds;
    private Map<Long,Integer> ordersProductCountsMap;

}
