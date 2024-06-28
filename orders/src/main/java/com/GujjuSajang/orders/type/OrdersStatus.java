package com.GujjuSajang.orders.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrdersStatus {

    COMPLETE("주문 완료"),
    DELIVERY("배송 중"),
    COMPLETED_DELIVERY("배송 완료"),
    CANCEL("취소"),
    RETURN_REQUEST("반품 신청"),
    RETURN_COMPLETED("반품 완료");

    private final String status;

}
