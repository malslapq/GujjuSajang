package com.GujjuSajang.core.dto;

import lombok.*;

import java.time.LocalDateTime;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SetProductSalesStartTimeDto {

    private Long productId;
    private Long memberId;
    private LocalDateTime startTime;

}
