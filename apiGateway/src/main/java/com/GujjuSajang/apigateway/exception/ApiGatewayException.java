package com.GujjuSajang.apigateway.exception;

import lombok.Getter;

@Getter
public class ApiGatewayException extends RuntimeException{

    private final ErrorCode errorCode;
    private final int status;
    private final String errorMessage;

    public ApiGatewayException(ErrorCode errorCode) {
        this(errorCode, null);
    }

    public ApiGatewayException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getErrorMessage(), cause);
        this.errorCode = errorCode;
        this.status = errorCode.getHttpStatus().value();
        this.errorMessage = errorCode.getErrorMessage();
    }

}
