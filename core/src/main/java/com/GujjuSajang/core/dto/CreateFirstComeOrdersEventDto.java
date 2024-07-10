package com.GujjuSajang.core.dto;

import lombok.*;

import javax.print.attribute.standard.PrinterURI;

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
