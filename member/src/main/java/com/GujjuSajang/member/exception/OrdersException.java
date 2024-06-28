package com.GujjuSajang.member.exception;

import lombok.Getter;

@Getter
public class OrdersException extends RuntimeException{

    private final ErrorCode errorCode;
    private final int status;
    private final String errorMessage;

    public OrdersException(ErrorCode errorCode) {
        this(errorCode, null);
    }

    public OrdersException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getErrorMessage(), cause);
        this.errorCode = errorCode;
        this.status = errorCode.getHttpStatus().value();
        this.errorMessage = errorCode.getErrorMessage();
    }

}
