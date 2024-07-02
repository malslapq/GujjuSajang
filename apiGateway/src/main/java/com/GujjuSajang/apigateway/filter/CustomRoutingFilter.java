package com.GujjuSajang.apigateway.filter;

import com.GujjuSajang.apigateway.dto.RequestHandlerDto;
import com.GujjuSajang.apigateway.exception.ApiGatewayException;
import com.GujjuSajang.apigateway.exception.ErrorCode;
import com.GujjuSajang.apigateway.handler.RequestHandler;
import com.GujjuSajang.apigateway.handler.RequestHandlerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import static com.GujjuSajang.apigateway.filter.JwtFilter.TOKEN_PATH;

@Order(2)
@Component
@RequiredArgsConstructor
public class CustomRoutingFilter implements WebFilter {

    public static final String MEMBER_SERVICE = "MEMBER";
    public static final String OTHER_SERVICE = "OTHER";

    private final RequestHandlerFactory requestHandlerFactory;


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String requestURI = exchange.getRequest().getPath().toString(); // 요청 URI

        if (requestURI.startsWith(TOKEN_PATH)) {
            return chain.filter(exchange);
        }

        String serviceName = getServiceName(requestURI);
        RequestHandler handler = requestHandlerFactory.getRequestHandler(serviceName);

        RequestHandlerDto requestHandlerDto = RequestHandlerDto.builder()
                .exchange(exchange)
                .filterChain(chain)
                .requestURI(requestURI)
                .serviceURI(getServiceUri(serviceName, requestURI))
                .build();

        return handler.handleRequest(requestHandlerDto);
    }


    private String getServiceName(String requestURI) {
        return switch (requestURI.split("/")[1]) {
            case "member" -> MEMBER_SERVICE;
            case "orders", "cart", "product" -> OTHER_SERVICE;
            default -> throw new ApiGatewayException(ErrorCode.INVALID_SERVICE_URI);
        };
    }

    private String getServiceUri(String serviceName, String requestURI) {
        return "http://" + serviceName + requestURI;
    }


}
