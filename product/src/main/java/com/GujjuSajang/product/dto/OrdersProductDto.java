package com.GujjuSajang.product.dto;


import com.GujjuSajang.core.type.OrdersStatus;
import lombok.*;

import java.time.LocalDateTime;


@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrdersProductDto {

    private Long id;
    private Long ordersId;
    private Long productId;
    private String name;
    private int count;
    private int Price;
    private OrdersStatus status;
    private LocalDateTime createAt;


}
