package com.GujjuSajang.core.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrdersStatus {

    PAYMENT_PENDING("결제 대기"),
    INSUFFICIENT_STOCK("주문 취소(재고 부족)"),
    COMPLETE("주문 완료"),
    PROCESSING_ERROR("주문 실패(서버 에러)"),
    DELIVERY("배송 중"),
    COMPLETED_DELIVERY("배송 완료"),
    CANCEL("주문 취소"),
    RETURN_REQUEST("반품 신청"),
    RETURN_COMPLETED("반품 완료");

    private final String status;

}
