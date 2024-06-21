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
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰");

    private final HttpStatus httpStatus;
    private final String errorMessage;

}
