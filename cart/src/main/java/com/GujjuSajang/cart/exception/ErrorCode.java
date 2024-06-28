package com.GujjuSajang.cart.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 재고 관련
    NOT_FOUND_STOCK(HttpStatus.NOT_FOUND, "해당 제품의 재고 정보를 찾을 수 없습니다."),

    // 주문 관련
    NOT_ENOUGH_STOCK(HttpStatus.BAD_REQUEST, "상품의 재고가 부족합니다."),
    NOT_FOUND_ORDER_PRODUCT(HttpStatus.NOT_FOUND, "결제 할 제품이 존재하지 않습니다."),
    NOT_FOUND_ORDERED_PRODUCT(HttpStatus.NOT_FOUND,"주문한 제품을 찾을 수 없습니다."),
    ORDER_CANCELLATION_PERIOD_EXPIRED(HttpStatus.BAD_REQUEST, "주문을 취소할 수 있는 기간이 아닙니다."),
    RETURN_PERIOD_EXPIRED(HttpStatus.BAD_REQUEST, "반품할 수 있는 기간이 아닙니다."),
    ACTION_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "요청이 가능한 상태가 아닙니다."),


    // 레디스 관련
    REDIS_OPERATION_FAILURE(HttpStatus.INTERNAL_SERVER_ERROR, "Redis 작업 실패"),

    // 장바구니 관련
    INVALID_CART_UPDATE(HttpStatus.BAD_REQUEST, "잘못된 장바구니 제품 변경 요청"),
    CART_NOT_FOUND(HttpStatus.BAD_REQUEST, "장바구니를 찾을 수 없습니다."),

    // 상품 관련
    NOT_FOUND_PRODUCT(HttpStatus.NOT_FOUND, "상품을 찾을 수 없습니다."),
    MISS_MATCH_PRODUCT(HttpStatus.BAD_REQUEST, "수정하려는 상품이 일치하지 않습니다."),

    // 토큰 관련
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "토큰 유효 시간 만료"),
    UNSUPPORTED_TOKEN(HttpStatus.BAD_REQUEST, "지원하지 않는 토큰"),
    MALFORMED_TOKEN(HttpStatus.BAD_REQUEST, "토큰 형식이 잘못됨"),
    INVALID_SIGNATURE(HttpStatus.UNAUTHORIZED, "서명이 유효하지 않음"),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰"),

    // 회원 관련
    NOT_FOUND_MEMBER(HttpStatus.BAD_REQUEST, "회원이 존재하지 않습니다."),
    ALREADY_MAIL(HttpStatus.CONFLICT, "이미 사용중인 메일입니다."),
    MISS_MATCH_MEMBER(HttpStatus.BAD_REQUEST, "회원이 일치하지 않습니다."),
    MISS_MATCH_PASSWORD(HttpStatus.BAD_REQUEST, "비밀번호가 일치하지 않습니다."),
    INVALID_CODE(HttpStatus.UNAUTHORIZED, "유효하지 않은 코드"),
    MAIL_NOT_VERIFIED(HttpStatus.UNAUTHORIZED, "메일 인증이 필요합니다."),
    ROLE_NOT_ALLOWED(HttpStatus.FORBIDDEN, "권한이 없습니다."),
    FAIL_SEND_MAIL(HttpStatus.SERVICE_UNAVAILABLE, "일시적인 메일 전송 오류입니다." );

    private final HttpStatus httpStatus;
    private final String errorMessage;

}
