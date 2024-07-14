package com.GujjuSajang.webflux.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {


    // 재고 관련
    NOT_FOUND_STOCK(HttpStatus.NOT_FOUND, "해당 제품의 재고 정보를 찾을 수 없습니다."),
    NOT_SALES_TIME(HttpStatus.BAD_REQUEST, "해당 제품 판매 시간이 아닙니다."),

    // 레디스 관련
    REDIS_OPERATION_FAILURE(HttpStatus.INTERNAL_SERVER_ERROR, "Redis 작업 실패");

    private final HttpStatus httpStatus;
    private final String errorMessage;

}
