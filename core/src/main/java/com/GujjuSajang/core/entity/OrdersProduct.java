package com.GujjuSajang.core.entity;

import com.GujjuSajang.core.dto.CartProductsDto;
import com.GujjuSajang.core.type.OrdersStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@EntityListeners(AuditingEntityListener.class)
public class OrdersProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Long ordersId;
    @Column(nullable = false)
    private Long productId;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private int count;
    @Column(nullable = false)
    private int price;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private OrdersStatus status;
    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updateAt;

    public static OrdersProduct of(Long orderId, CartProductsDto cartProductsDto) {
        return OrdersProduct.builder()
                .ordersId(orderId)
                .productId(cartProductsDto.getProductId())
                .name(cartProductsDto.getName())
                .count(cartProductsDto.getCount())
                .price(cartProductsDto.getPrice() * cartProductsDto.getCount())
                .status(OrdersStatus.PAYMENT_PENDING)
                .build();
    }

    public Long updateDeliveryStatus() {
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
        return prevStatus != this.status && this.status == OrdersStatus.RETURN_COMPLETED ? this.getProductId() : -1;
    }

    public void changeStatus(OrdersStatus status) {
        this.status = status;
    }

}
