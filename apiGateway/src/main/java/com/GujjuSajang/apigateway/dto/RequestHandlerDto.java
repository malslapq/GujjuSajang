package com.GujjuSajang.apigateway.dto;

import lombok.*;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RequestHandlerDto {

    private ServerWebExchange exchange;
    private WebFilterChain filterChain;
    private String requestURI;
    private String serviceURI;

}
