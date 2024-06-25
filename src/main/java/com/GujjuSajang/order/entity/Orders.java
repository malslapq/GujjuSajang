package com.GujjuSajang.order.entity;

import com.GujjuSajang.cart.dto.CartDto;
import com.GujjuSajang.util.BaseTimeEntity;
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

    public static Orders from(Long memberId, CartDto cartDto) {
        return Orders.builder()
                .memberId(memberId)
                .totalPrice(cartDto.getCartProductsDtos().stream().mapToInt(cartProductsDto -> cartProductsDto.getPrice() * cartProductsDto.getCount()).sum())
                .build();
    }



}
