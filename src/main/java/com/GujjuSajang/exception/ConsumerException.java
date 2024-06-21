package com.GujjuSajang.exception;

import lombok.Getter;

@Getter
public class ConsumerException extends RuntimeException{

    private final ErrorCode errorCode;
    private final int status;
    private final String errorMessage;

    public ConsumerException(ErrorCode errorCode) {
        this(errorCode, null);
    }

    public ConsumerException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getErrorMessage(), cause);
        this.errorCode = errorCode;
        this.status = errorCode.getHttpStatus().value();
        this.errorMessage = errorCode.getErrorMessage();
    }
}
