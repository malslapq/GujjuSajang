package com.GujjuSajang.apigateway.handler;

import com.GujjuSajang.apigateway.dto.RequestHandlerDto;
import com.GujjuSajang.apigateway.dto.TokenMemberInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class OtherRequestHandler implements RequestHandler {

    private final WebClient.Builder webClientBuilder;

    @Override
    public Mono<Void> handleRequest(RequestHandlerDto requestHandlerDto) {

        ServerWebExchange exchange = requestHandlerDto.getExchange();

        TokenMemberInfo tokenMemberInfo = (TokenMemberInfo) requestHandlerDto.getExchange().getAttributes().get("tokenMemberInfo"); // TokenMemberInfo 가져오기
        return webClientBuilder.build()
                .post() // POST 요청
                .uri(requestHandlerDto.getServiceURI())
                .contentType(MediaType.APPLICATION_JSON) // 콘텐츠 타입
                .bodyValue(tokenMemberInfo) // 요청 바디 설정
                .retrieve() // 요청 보내기
                .bodyToMono(String.class) // 응답 바디 문자열로
                .flatMap(responseBody -> {
                    exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON); // 응답 헤더 json으로 설정
                    return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(responseBody.getBytes()))); // 응답 바디
                });
    }


}
