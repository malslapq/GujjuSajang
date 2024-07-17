package com.GujjuSajang.apigateway.filter;

import com.GujjuSajang.apigateway.dto.MemberLoginDto;
import com.GujjuSajang.apigateway.dto.TokenInfo;
import com.GujjuSajang.apigateway.dto.TokenMemberInfo;
import com.GujjuSajang.apigateway.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Order(2)
@Component
@RequiredArgsConstructor
public class CustomRoutingFilter implements WebFilter {

    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;
    private final AuthService authService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        if (exchange.getRequest().getPath().toString().equals("/member/login")) {
            return exchange.getRequest().getBody().next().flatMap(dataBuffer -> {
                // 요청 본문을 읽어 LoginRequest로 변환
                byte[] bytes = new byte[dataBuffer.readableByteCount()];
                dataBuffer.read(bytes);
                String body = new String(bytes);

                // WebClient 인스턴스 생성
                WebClient webClient = webClientBuilder.build();

                // 로그인 요청을 멤버 서비스로 전달
                return webClient.post()
                        .uri("http://member/login") // Eureka를 통해 member 서비스로 요청
                        .contentType(MediaType.APPLICATION_JSON) // 콘텐츠 타입 설정
                        .bodyValue(body)
                        .retrieve()
                        .bodyToMono(String.class)
                        .flatMap(responseBody -> {
                            ServerHttpResponse response = exchange.getResponse();
                            response.getHeaders().add(HttpHeaders.CONTENT_TYPE, "application/json");
                            try {
                                // 멤버 서비스의 응답을 TokenMemberInfo로 변환
                                MemberLoginDto.Response memberLoginResponse = objectMapper.readValue(responseBody, MemberLoginDto.Response.class);

                                // JWT 토큰 생성
                                TokenInfo tokenInfo = authService.issueTokens(TokenMemberInfo.builder()
                                        .id(memberLoginResponse.getId())
                                        .mail(memberLoginResponse.getMail())
                                        .role(memberLoginResponse.getRole())
                                        .mailVerified(memberLoginResponse.isMailVerified())
                                        .build());

                                // 쿠키 설정
                                ResponseCookie accessTokenCookie = ResponseCookie.from("accessToken", tokenInfo.getAccessToken())
                                        .httpOnly(true)
                                        .secure(true)
                                        .path("/")
                                        .maxAge(30 * 60)  // 30 minutes
                                        .build();
                                response.addCookie(accessTokenCookie);

                                // 응답 바디에 TokenInfo 넣기
                                byte[] responseBytes = objectMapper.writeValueAsBytes(tokenInfo);
                                return response.writeWith(Mono.just(response.bufferFactory().wrap(responseBytes)));
                            } catch (Exception e) {
                                response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
                                return response.setComplete();
                            }
                        });
            });
        }
        return chain.filter(exchange);
    }
}
