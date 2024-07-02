package com.GujjuSajang.apigateway.handler;

import com.GujjuSajang.apigateway.dto.RequestHandlerDto;
import reactor.core.publisher.Mono;

public interface RequestHandler {

    Mono<Void> handleRequest(RequestHandlerDto requestHandlerDto);

}
