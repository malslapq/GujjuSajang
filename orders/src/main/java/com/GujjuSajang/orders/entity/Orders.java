package com.GujjuSajang.orders.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class Orders {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Long memberId;
    @Column(nullable = false)
    private int totalPrice;
    @Column(updatable = false, nullable = false)
    private LocalDateTime createAt;
    @LastModifiedDate
    @Column
    private LocalDateTime updateAt;

//    public static Orders from(Long memberId, CartDto cartDto) {
//        return Orders.builder()
//                .memberId(memberId)
//                .totalPrice(cartDto.getCartProductsDtos().stream().mapToInt(cartProductsDto -> cartProductsDto.getPrice() * cartProductsDto.getCount()).sum())
//                .build();
//    }


}
