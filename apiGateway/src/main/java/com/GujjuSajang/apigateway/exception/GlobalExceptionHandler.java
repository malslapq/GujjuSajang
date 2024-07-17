package com.GujjuSajang.apigateway.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiGatewayException.class)
    public ResponseEntity<ErrorResponse> handleMemberException(ApiGatewayException exception) {
        ErrorResponse errorResponse = new ErrorResponse(exception.getStatus(), exception.getErrorMessage(), exception.getErrorCode().name());
        return ResponseEntity.status(exception.getStatus()).body(errorResponse);
    }

    @ExceptionHandler(MemberException.class)
    public ResponseEntity<ErrorResponse> handleMemberException(MemberException exception) {
        ErrorResponse errorResponse = new ErrorResponse(exception.getStatus(), exception.getErrorMessage(), exception.getErrorCode().name());
        return ResponseEntity.status(exception.getStatus()).body(errorResponse);
    }

    @ExceptionHandler(TokenException.class)
    public ResponseEntity<ErrorResponse> handleTokenExceptionException(TokenException exception) {
        ErrorResponse errorResponse = new ErrorResponse(exception.getStatus(), exception.getErrorMessage(), exception.getErrorCode().name());
        return ResponseEntity.status(exception.getStatus()).body(errorResponse);
    }

    @ExceptionHandler(RedisException.class)
    public ResponseEntity<ErrorResponse> handleMemberException(RedisException exception) {
        ErrorResponse errorResponse = new ErrorResponse(exception.getStatus(), exception.getErrorMessage(), exception.getErrorCode().name());
        return ResponseEntity.status(exception.getStatus()).body(errorResponse);
    }

}
