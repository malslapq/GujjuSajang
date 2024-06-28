package com.GujjuSajang.core.exception;

import lombok.Getter;

@Getter
public class RedisException extends RuntimeException{

    private final ErrorCode errorCode;
    private final int status;
    private final String errorMessage;

    public RedisException(ErrorCode errorCode) {
        this(errorCode, null);
    }

    public RedisException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getErrorMessage(), cause);
        this.errorCode = errorCode;
        this.status = errorCode.getHttpStatus().value();
        this.errorMessage = errorCode.getErrorMessage();
    }

}
