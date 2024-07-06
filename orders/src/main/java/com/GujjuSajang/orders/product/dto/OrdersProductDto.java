package com.GujjuSajang.orders.product.dto;


import com.GujjuSajang.core.type.DeliveryStatus;
import com.GujjuSajang.orders.product.entity.OrdersProduct;
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
    private DeliveryStatus status;
    private LocalDateTime createAt;

    public static OrdersProductDto from(OrdersProduct ordersProduct) {
        return OrdersProductDto.builder()
                .id(ordersProduct.getId())
                .ordersId(ordersProduct.getOrdersId())
                .productId(ordersProduct.getProductId())
                .name(ordersProduct.getName())
                .count(ordersProduct.getCount())
                .Price(ordersProduct.getPrice())
                .status(ordersProduct.getStatus())
                .createAt(ordersProduct.getCreatedAt())
                .build();
    }

}
