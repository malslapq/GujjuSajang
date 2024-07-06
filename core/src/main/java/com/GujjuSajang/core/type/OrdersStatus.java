package com.GujjuSajang.core.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrdersStatus {

    PAYMENT_PENDING("결제 대기"),
    COMPLETE("주문 완료"),
    PROCESSING_ERROR("주문 실패");

    private final String status;

}
