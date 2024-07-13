package com.GujjuSajang.core.exception;

import lombok.Getter;

@Getter
public class PaymentException extends RuntimeException{

    private final ErrorCode errorCode;
    private final int status;
    private final String errorMessage;

    public PaymentException(ErrorCode errorCode) {
        this(errorCode, null);
    }

    public PaymentException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getErrorMessage(), cause);
        this.errorCode = errorCode;
        this.status = errorCode.getHttpStatus().value();
        this.errorMessage = errorCode.getErrorMessage();
    }

}