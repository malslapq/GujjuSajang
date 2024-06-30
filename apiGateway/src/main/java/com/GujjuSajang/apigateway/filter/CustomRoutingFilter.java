package com.GujjuSajang.apigateway.filter;

import com.GujjuSajang.apigateway.dto.TokenInfo;
import com.GujjuSajang.apigateway.dto.TokenMemberInfo;
import com.GujjuSajang.apigateway.exception.ApiGatewayException;
import com.GujjuSajang.apigateway.exception.ErrorCode;
import com.GujjuSajang.apigateway.service.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.time.Duration;

import static com.GujjuSajang.apigateway.filter.JwtFilter.LOGIN_PATH;
import static com.GujjuSajang.apigateway.filter.JwtFilter.TOKEN_PATH;
import static com.GujjuSajang.apigateway.util.JwtUtil.COOKIE_NAME;

@Order(2)
@Component
@RequiredArgsConstructor
public class CustomRoutingFilter implements WebFilter {

    private final WebClient.Builder webClientBuilder;
    private final JwtService jwtService;
    private final ObjectMapper objectMapper;
    private static final String MEMBER_SERVICE = "MEMBER";
    private static final String ORDER_SERVICE = "ORDERS";
    private static final String PRODUCT_SERVICE = "PRODUCT";
    private static final String CART_SERVICE = "CART";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String requestURI = exchange.getRequest().getPath().toString(); // 요청 URI 가져오기

        if (requestURI.startsWith(TOKEN_PATH)) { //
            return chain.filter(exchange); //
        }

        if (requestURI.startsWith(LOGIN_PATH)) { //
            return loginRequest(exchange); // 로그인 요청 처리
        }

        return otherRequests(exchange, chain, requestURI); // 일반 요청 처리
    }

    // 로그인 요청 받아서 member에게 전달 후 다시 클라이언트에게 응답 처리
    private Mono<Void> loginRequest(ServerWebExchange exchange) {
        String serviceUri = getServiceUri(exchange.getRequest().getPath().toString());
        ServerHttpResponse response = exchange.getResponse();
        return webClientBuilder.build().post().uri(serviceUri).bodyValue(exchange.getRequest().getBody()).retrieve() // 들어온 요청 그대로 member쪽으로 보냄
                .bodyToMono(TokenMemberInfo.class)
                .flatMap(tokenMemberInfo -> {
                    TokenInfo tokenInfo = jwtService.issueTokens(tokenMemberInfo);
                    createAccessTokenCookie(tokenInfo.getAccessToken(), response);
                    exchange.getResponse().getHeaders().add("Content-Type", "application/json");
                    try {
                        String jsonResponse = objectMapper.writeValueAsString(tokenInfo); // TokenInfo를 JSON 문자열로 변환
                        return exchange.getResponse().writeWith(Mono.just(exchange.getResponse()
                                .bufferFactory()
                                .wrap(jsonResponse.getBytes()))); // 응답 본문 설정
                    } catch (Exception e) {
                        return Mono.error(new ApiGatewayException(ErrorCode.INTERNAL_SERVER_ERROR, e));
                    }
                });
    }

    private Mono<Void> otherRequests(ServerWebExchange exchange, WebFilterChain chain, String requestURI) {
        TokenMemberInfo tokenMemberInfo = (TokenMemberInfo) exchange.getAttributes().get("tokenMemberInfo"); // TokenMemberInfo 가져오기
        String serviceUri = getServiceUri(requestURI); // 서비스 URI 가져오기
        return webClientBuilder.build()
                .post() // POST 요청 설정
                .uri(serviceUri) // 서비스 URI 설정
                .bodyValue(tokenMemberInfo) // 요청 본문 설정
                .retrieve() // 요청 보내기
                .bodyToMono(String.class) // 응답 본문을 문자열로 변환
                .flatMap(responseBody -> {
                    exchange.getResponse().getHeaders().add("Content-Type", "application/json"); // 응답 헤더 설정
                    return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(responseBody.getBytes()))); // 응답 본문 설정
                });
    }

    private String getServiceUri(String requestURI) {
        String serviceName = switch (requestURI.split("/")[1]) {
            case "member" -> MEMBER_SERVICE;
            case "order" -> ORDER_SERVICE;
            case "cart" -> CART_SERVICE;
            case "product" -> PRODUCT_SERVICE;
            default -> throw new ApiGatewayException(ErrorCode.INVALID_SERVICE_URI);
        };
        return "http://" + serviceName + requestURI;
    }

    private void createAccessTokenCookie(String accessToken, ServerHttpResponse response) {
        ResponseCookie cookie = ResponseCookie.from(COOKIE_NAME, accessToken)
                .httpOnly(true) // HTTP 전용 설정
                .secure(true) // 보안 설정
                .path("/") // 경로 설정
                .maxAge(Duration.ofMinutes(30)) // 유효 기간 설정
                .build();// 쿠키 빌드
        response.addCookie(cookie);
    }

}
