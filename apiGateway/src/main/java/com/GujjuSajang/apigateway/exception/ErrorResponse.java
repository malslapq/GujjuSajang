package com.GujjuSajang.apigateway.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse {

    private int status;
    private String message;
    private String errorCode;

}
