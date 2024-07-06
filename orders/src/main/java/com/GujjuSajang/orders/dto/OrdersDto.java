package com.GujjuSajang.orders.dto;

import com.GujjuSajang.core.type.OrdersStatus;
import com.GujjuSajang.orders.entity.Orders;
import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class OrdersDto {

    private Long id;
    private int totalPrice;
    private LocalDateTime createdAt;
    private OrdersStatus status;

    public static OrdersDto from(Orders orders) {
        return OrdersDto.builder()
                .id(orders.getId())
                .totalPrice(orders.getTotalPrice())
                .status(orders.getStatus())
                .createdAt(orders.getCreatedAt())
                .build();
    }

}
