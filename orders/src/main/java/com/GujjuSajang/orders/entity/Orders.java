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
    private Long paymentId;
    @Column(nullable = false)
    private int totalPrice;
    @Enumerated(EnumType.STRING)
    private OrdersStatus status;

    public static Orders of(CreateOrderEventDto createOrderEventDto, OrdersStatus status) {
        return Orders.builder()
                .memberId(createOrderEventDto.getMemberId())
                .paymentId(createOrderEventDto.getPaymentId())
                .totalPrice(createOrderEventDto.getCartProductsDtos().stream().mapToInt(cartProductsDto -> cartProductsDto.getPrice() * cartProductsDto.getCount()).sum())
                .status(status)
                .build();
    }

}
