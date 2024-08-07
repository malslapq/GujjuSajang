package com.GujjuSajang.apigateway.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // api-gateway 관련
    INVALID_SERVICE_URI(HttpStatus.BAD_REQUEST, "잘못된 요청 URI 입니다"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 문제"),

    // 레디스 관련
    REDIS_OPERATION_FAILURE(HttpStatus.INTERNAL_SERVER_ERROR, "Redis 작업 실패"),

    // 토큰 관련
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "토큰 유효 시간 만료"),
    UNSUPPORTED_TOKEN(HttpStatus.BAD_REQUEST, "지원하지 않는 토큰"),
    MALFORMED_TOKEN(HttpStatus.BAD_REQUEST, "토큰 형식이 잘못됨"),
    INVALID_SIGNATURE(HttpStatus.UNAUTHORIZED, "서명이 유효하지 않음"),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰"),
    MISSING_AUTHORIZATION_HEADER(HttpStatus.BAD_REQUEST, "Authorization Header 가 존재하지 않습니다."),

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
