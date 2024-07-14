package com.GujjuSajang.webflux.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ProductException.class)
    public ResponseEntity<ErrorResponse> handleProductExceptionException(ProductException exception) {
        ErrorResponse errorResponse = new ErrorResponse(exception.getStatus(), exception.getErrorMessage(), exception.getErrorCode().name());
        return ResponseEntity.status(exception.getStatus()).body(errorResponse);
    }

    @ExceptionHandler(RedisException.class)
    public ResponseEntity<ErrorResponse> handleMemberException(RedisException exception) {
        ErrorResponse errorResponse = new ErrorResponse(exception.getStatus(), exception.getErrorMessage(), exception.getErrorCode().name());
        return ResponseEntity.status(exception.getStatus()).body(errorResponse);
    }

}
