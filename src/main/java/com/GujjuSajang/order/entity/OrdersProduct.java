package com.GujjuSajang.order.entity;

import com.GujjuSajang.cart.dto.CartProductsDto;
import com.GujjuSajang.order.type.OrdersStatus;
import com.GujjuSajang.util.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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

    public boolean changeDeliveryStatus() {
        LocalDateTime now = LocalDateTime.now();
        OrdersStatus prevStatus = this.status;
        switch (this.status) {
            case COMPLETE:
                this.status = now.isAfter(this.getUpdateAt().plusDays(1)) ? OrdersStatus.DELIVERY : OrdersStatus.COMPLETE;
                break;
            case DELIVERY:
                this.status = now.isAfter(this.getUpdateAt().plusDays(1)) ? OrdersStatus.COMPLETED_DELIVERY : OrdersStatus.DELIVERY;
                break;
            case RETURN_REQUEST:
                this.status = now.isAfter(this.getUpdateAt().plusDays(1)) ? OrdersStatus.RETURN_COMPLETED : OrdersStatus.RETURN_REQUEST;
                break;
        }
        return prevStatus != this.status && this.status == OrdersStatus.RETURN_COMPLETED;
    }

    public void changeStatus(OrdersStatus status) {
        this.status = status;
    }

}
