package com.GujjuSajang.core.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DeliveryStatus {

    COMPLETE("주문 완료"),
    DELIVERY("배송 중"),
    COMPLETED_DELIVERY("배송 완료"),
    RETURN_REQUEST("반품 신청"),
    CANCEL("주문 취소"),
    PROCESSING_ERROR("변경 실패 서버 에러"),
    RETURN_COMPLETED("반품 완료");

    private final String status;
}
