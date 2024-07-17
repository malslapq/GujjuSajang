package com.GujjuSajang.core.dto;

import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateFirstComeOrdersEventDto {

    private Long ordersId;
    private Long memberId;
    private CartProductsDto cartProductsDto;

}
