package com.GujjuSajang.core.dto;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateOrderEventDto {

    private Long memberId;
    private Long orderId;
    private Long paymentId;
    private List<CartProductsDto> cartProductsDtos = new ArrayList<>();
}

