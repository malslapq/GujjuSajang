package com.GujjuSajang.orders.dto;

import com.GujjuSajang.orders.entity.OrdersProduct;
import com.GujjuSajang.orders.type.OrdersStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;


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

    public static OrdersProductDto from(OrdersProduct ordersProduct, Map<Long, String> productNameMap) {
        return OrdersProductDto.builder()
                .id(ordersProduct.getId())
                .ordersId(ordersProduct.getOrdersId())
                .productId(ordersProduct.getProductId())
                .count(ordersProduct.getCount())
                .name(productNameMap.get(ordersProduct.getProductId()))
                .Price(ordersProduct.getPrice())
                .status(ordersProduct.getStatus())
                .createAt(ordersProduct.getCreateAt())
                .build();
    }

//    public static OrdersProductDto from(OrdersProduct ordersProduct, Product product) {
//        return OrdersProductDto.builder()
//                .id(ordersProduct.getId())
//                .ordersId(ordersProduct.getOrdersId())
//                .productId(ordersProduct.getProductId())
//                .count(ordersProduct.getCount())
//                .name(product.getName())
//                .Price(ordersProduct.getPrice())
//                .status(ordersProduct.getStatus())
//                .createAt(ordersProduct.getCreateAt())
//                .build();
//    }

}
