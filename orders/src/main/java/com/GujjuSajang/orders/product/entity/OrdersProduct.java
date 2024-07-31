package com.GujjuSajang.orders.product.entity;

import com.GujjuSajang.core.dto.CartProductsDto;
import com.GujjuSajang.core.type.DeliveryStatus;
import com.GujjuSajang.orders.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@EntityListeners(AuditingEntityListener.class)
public class OrdersProduct extends BaseTimeEntity {

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
    private DeliveryStatus status;

    public static OrdersProduct of(Long orderId, CartProductsDto cartProductsDto, DeliveryStatus deliveryStatus) {
        return OrdersProduct.builder()
                .ordersId(orderId)
                .productId(cartProductsDto.getProductId())
                .name(cartProductsDto.getName())
                .count(cartProductsDto.getCount())
                .price(cartProductsDto.getPrice() * cartProductsDto.getCount())
                .status(deliveryStatus)
                .build();
    }

    public Long updateDeliveryStatus() {
        LocalDateTime now = LocalDateTime.now();
        DeliveryStatus prevStatus = this.status;
        switch (this.status) {
            case COMPLETE:
                this.status = now.isAfter(this.getUpdateAt().plusDays(1)) ? DeliveryStatus.DELIVERY : DeliveryStatus.COMPLETE;
                break;
            case DELIVERY:
                this.status = now.isAfter(this.getUpdateAt().plusDays(1)) ? DeliveryStatus.COMPLETED_DELIVERY : DeliveryStatus.DELIVERY;
                break;
            case RETURN_REQUEST:
                this.status = now.isAfter(this.getUpdateAt().plusDays(1)) ? DeliveryStatus.RETURN_COMPLETED : DeliveryStatus.RETURN_REQUEST;
                break;
        }
        return prevStatus != this.status && this.status == DeliveryStatus.RETURN_COMPLETED ? this.getProductId() : -1;
    }

    public void changeStatus(DeliveryStatus status) {
        this.status = status;
    }

    public void rollBackReturnCompletedStatus() {
        this.status = DeliveryStatus.RETURN_REQUEST;
    }

    public void processingErrorStatus() {
        this.status = DeliveryStatus.PROCESSING_ERROR;
    }

}
