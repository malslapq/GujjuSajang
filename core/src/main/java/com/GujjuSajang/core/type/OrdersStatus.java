package com.GujjuSajang.core.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrdersStatus {

    PAYMENT_PENDING("결제 대기"),
    PROCESSING_ERROR("주문 실패(서버 오류)"),
    FAILED_PAYMENT("결제 실패"),
    COMPLETE("주문 완료");

    private final String status;

}
