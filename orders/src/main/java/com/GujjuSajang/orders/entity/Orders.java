package com.GujjuSajang.orders.entity;

import com.GujjuSajang.core.dto.CreateOrderEventDto;
import com.GujjuSajang.core.type.OrdersStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class Orders extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Long memberId;
    @Column(nullable = false)
    private int totalPrice;
    @Enumerated(EnumType.STRING)
    private OrdersStatus status;

    public static Orders from(CreateOrderEventDto createOrderEventDto) {
        return Orders.builder()
                .memberId(createOrderEventDto.getMemberId())
                .totalPrice(createOrderEventDto.getCartProductsDtos().stream().mapToInt(cartProductsDto -> cartProductsDto.getPrice() * cartProductsDto.getCount()).sum())
                .status(OrdersStatus.PAYMENT_PENDING)
                .build();
    }

    public void successOrders() {
        this.status = OrdersStatus.COMPLETE;
    }

    public void failOrdersFromPayment() {
        this.status = OrdersStatus.FAILED_PAYMENT;
    }

    public void failOrdersFromOrdersProducts() {
        this.status = OrdersStatus.PROCESSING_ERROR;
    }


}
