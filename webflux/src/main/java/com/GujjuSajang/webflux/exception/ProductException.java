package com.GujjuSajang.webflux.exception;

import lombok.Getter;

@Getter
public class ProductException extends RuntimeException{

    private final ErrorCode errorCode;
    private final int status;
    private final String errorMessage;

    public ProductException(ErrorCode errorCode) {
        this(errorCode, null);
    }

    public ProductException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getErrorMessage(), cause);
        this.errorCode = errorCode;
        this.status = errorCode.getHttpStatus().value();
        this.errorMessage = errorCode.getErrorMessage();
    }

}
