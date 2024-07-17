package com.GujjuSajang.core.dto;

import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateStockDto {

    private Long productId;
    private int count;

}
