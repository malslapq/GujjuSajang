package com.GujjuSajang.order.entity;

import com.GujjuSajang.cart.dto.CartProductsDto;
import com.GujjuSajang.order.type.OrdersStatus;
import com.GujjuSajang.util.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class OrdersProduct extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Long ordersId;
    @Column(nullable = false)
    private Long productId;
    @Column(nullable = false)
    private int count;
    @Column(nullable = false)
    private int price;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private OrdersStatus status;

    public static OrdersProduct from(Long orderId, CartProductsDto cartProductsDto) {
        return OrdersProduct.builder()
                .ordersId(orderId)
                .productId(cartProductsDto.getProductID())
                .count(cartProductsDto.getCount())
                .price(cartProductsDto.getPrice() * cartProductsDto.getCount())
                .status(OrdersStatus.COMPLETE)
                .build();
    }

    public void changeStatus(OrdersStatus status) {
        this.status = status;
    }

}
