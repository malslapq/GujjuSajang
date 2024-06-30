package com.GujjuSajang.apigateway.filter;

import com.GujjuSajang.apigateway.dto.TokenMemberInfo;
import com.GujjuSajang.apigateway.exception.ApiGatewayException;
import com.GujjuSajang.apigateway.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import static com.GujjuSajang.apigateway.filter.JwtFilter.TOKEN_PATH;

@Order(2)
@Component
@RequiredArgsConstructor
public class CustomRoutingFilter implements WebFilter {

    private final WebClient.Builder webClientBuilder;
    private static final String MEMBER_SERVICE = "MEMBER";
    private static final String ORDER_SERVICE = "ORDERS";
    private static final String PRODUCT_SERVICE = "PRODUCT";
    private static final String CART_SERVICE = "CART";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String requestURI = exchange.getRequest().getPath().toString();
        if (requestURI.startsWith(TOKEN_PATH)) {
            return chain.filter(exchange);
        }
        TokenMemberInfo tokenMemberInfo = (TokenMemberInfo) exchange.getAttributes().get("tokenMemberInfo");

        String serviceUri = getServiceUri(requestURI);
        return webClientBuilder.build() // WebClient 빌더로부터 WebClient 객체를 생성
                .post() // HTTP POST 요청을 준비
                .uri(serviceUri) // 동적으로 서비스 URI 설정
                .bodyValue(tokenMemberInfo) // 요청 본문에 tokenMemberInfo 객체 추가
                .retrieve() // 요청을 보내고 응답을 수신
                .bodyToMono(Void.class) // 응답 본문을 Mono<Void>로 변환
                .then(chain.filter(exchange)); // WebFilterChain을 통해 다음 필터로 전달
    }

    private String getServiceUri(String requestURI) {
        String serviceName;
        switch (requestURI.split("/")[1]) {
            case "member":
                serviceName = MEMBER_SERVICE;
                break;
            case "order":
                serviceName = ORDER_SERVICE;
                break;
            case "cart":
                serviceName = CART_SERVICE;
                break;
            case "product":
                serviceName = PRODUCT_SERVICE;
                break;
            default:
                throw new ApiGatewayException(ErrorCode.INVALID_SERVICE_URI);
        }
        return "http://" + serviceName + requestURI;
    }
}
