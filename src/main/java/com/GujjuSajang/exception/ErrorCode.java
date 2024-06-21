package com.GujjuSajang.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "토큰 유효 시간 만료"),
    UNSUPPORTED_TOKEN(HttpStatus.BAD_REQUEST, "지원하지 않는 토큰"),
    MALFORMED_TOKEN(HttpStatus.BAD_REQUEST, "토큰 형식이 잘못됨"),
    INVALID_SIGNATURE(HttpStatus.UNAUTHORIZED, "서명이 유효하지 않음"),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰"),
    NOT_FOUND_CONSUMER(HttpStatus.BAD_REQUEST, "구매회원이 존재하지 않습니다."),
    MISS_MATCH_CONSUMER(HttpStatus.BAD_REQUEST, "변경하려는 회원과 일치하지 않습니다."),
    MISS_MATCH_PASSWORD(HttpStatus.BAD_REQUEST, "비밀번호가 일치하지 않습니다."),
    INVALID_CODE(HttpStatus.UNAUTHORIZED, "유효하지 않은 코드"),
    FAIL_SEND_MAIL(HttpStatus.SERVICE_UNAVAILABLE, "일시적인 메일 전송 오류입니다." );

    private final HttpStatus httpStatus;
    private final String errorMessage;

}
