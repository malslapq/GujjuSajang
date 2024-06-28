package com.GujjuSajang.core.exception;

import lombok.Getter;

@Getter
public class CartException extends RuntimeException{

    private final ErrorCode errorCode;
    private final int status;
    private final String errorMessage;

    public CartException(ErrorCode errorCode) {
        this(errorCode, null);
    }

    public CartException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getErrorMessage(), cause);
        this.errorCode = errorCode;
        this.status = errorCode.getHttpStatus().value();
        this.errorMessage = errorCode.getErrorMessage();
    }
}