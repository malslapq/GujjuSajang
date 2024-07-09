package com.GujjuSajang.payment.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentStatus {

    FAILED("결제 실패"),
    CANCEL("결제 취소"),
    COMPLETED("결제 완료");

    private final String status;
}
