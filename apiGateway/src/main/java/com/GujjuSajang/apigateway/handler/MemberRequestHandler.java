package com.GujjuSajang.apigateway.handler;

import com.GujjuSajang.apigateway.dto.MemberSignUpDto;
import com.GujjuSajang.apigateway.dto.RequestHandlerDto;
import com.GujjuSajang.apigateway.dto.TokenInfo;
import com.GujjuSajang.apigateway.dto.TokenMemberInfo;
import com.GujjuSajang.apigateway.exception.ApiGatewayException;
import com.GujjuSajang.apigateway.exception.ErrorCode;
import com.GujjuSajang.apigateway.service.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static com.GujjuSajang.apigateway.filter.JwtFilter.LOGIN_PATH;
import static com.GujjuSajang.apigateway.filter.JwtFilter.SIGNUP_PATH;
import static com.GujjuSajang.apigateway.util.JwtUtil.COOKIE_NAME;

@Component
@RequiredArgsConstructor
public class MemberRequestHandler implements RequestHandler {

    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;
    private final JwtService jwtService;

    @Override
    public Mono<Void> handleRequest(RequestHandlerDto requestHandlerDto) {

        ServerWebExchange exchange = requestHandlerDto.getExchange();

        if (requestHandlerDto.getRequestURI().startsWith(LOGIN_PATH)) {
            return loginRequest(exchange, requestHandlerDto.getServiceURI()); // 로그인 요청 처리
        }

        if (requestHandlerDto.getRequestURI().startsWith(SIGNUP_PATH)) {
            return signupRequest(exchange, requestHandlerDto.getServiceURI()); // 회원가입 요청 처리
        }

        return null;
    }

    // 회원가입 요청 처리
    private Mono<Void> signupRequest(ServerWebExchange exchange, String serviceURI) {
        return DataBufferUtils.join(exchange.getRequest().getBody())
                .flatMap(dataBuffer -> {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    DataBufferUtils.release(dataBuffer);
                    String body = new String(bytes, StandardCharsets.UTF_8);
                    return webClientBuilder.build()
                            .post()
                            .uri(serviceURI)
                            .contentType(MediaType.APPLICATION_JSON) // 콘텐츠 타입 설정
                            .body(BodyInserters.fromValue(body))
                            .retrieve()
                            .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> {
                                if (clientResponse.statusCode() == HttpStatus.CONFLICT) {
                                    return clientResponse.bodyToMono(String.class).flatMap(errorBody -> {
                                        exchange.getResponse().setStatusCode(HttpStatus.CONFLICT);
                                        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
                                        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(errorBody.getBytes(StandardCharsets.UTF_8));
                                        return exchange.getResponse().writeWith(Mono.just(buffer)).then(Mono.error(new WebClientResponseException(
                                                HttpStatus.CONFLICT.value(), HttpStatus.CONFLICT.getReasonPhrase(), null, null, null)));
                                    });
                                }
                                return clientResponse.createException().flatMap(Mono::error);
                            })
                            .bodyToMono(MemberSignUpDto.class)
                            .flatMap(signUpResponse -> {
                                try {
                                    String jsonResponse = objectMapper.writeValueAsString(signUpResponse); // MemberSignUpDto를 JSON 문자열로 변환
                                    exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON); // 응답 헤더 json으로 설정
                                    DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(jsonResponse.getBytes(StandardCharsets.UTF_8));
                                    return exchange.getResponse().writeWith(Mono.just(buffer));
                                } catch (Exception e) {
                                    return Mono.error(new ApiGatewayException(ErrorCode.INTERNAL_SERVER_ERROR, e));
                                }
                            });
                });
    }

    // 로그인
    private Mono<Void> loginRequest(ServerWebExchange exchange, String serviceURI) {
        ServerHttpResponse response = exchange.getResponse();
        return DataBufferUtils.join(exchange.getRequest().getBody())
                .flatMap(dataBuffer -> {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    DataBufferUtils.release(dataBuffer);
                    String body = new String(bytes, StandardCharsets.UTF_8);
                    return webClientBuilder.build()
                            .post()
                            .uri(serviceURI)
                            .contentType(MediaType.APPLICATION_JSON) // 콘텐츠 타입 설정
                            .body(BodyInserters.fromValue(body))
                            .retrieve() // 들어온 요청 그대로 member쪽으로 보냄
                            .bodyToMono(TokenMemberInfo.class)
                            .flatMap(tokenMemberInfo -> {
                                TokenInfo tokenInfo = jwtService.issueTokens(tokenMemberInfo);
                                createAccessTokenCookie(tokenInfo.getAccessToken(), response);
                                exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON); // 응답 헤더 json으로 설정
                                try {
                                    String jsonResponse = objectMapper.writeValueAsString(tokenInfo); // TokenInfo를 JSON 문자열로 변환
                                    return exchange.getResponse().writeWith(Mono.just(exchange.getResponse()
                                            .bufferFactory()
                                            .wrap(jsonResponse.getBytes()))); // 응답 본문 설정
                                } catch (Exception e) {
                                    return Mono.error(new ApiGatewayException(ErrorCode.INTERNAL_SERVER_ERROR, e));
                                }
                            });
                });
    }

    private void createAccessTokenCookie(String accessToken, ServerHttpResponse response) {
        ResponseCookie cookie = ResponseCookie.from(COOKIE_NAME, accessToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(Duration.ofMinutes(30)) // 유효기간 30분
                .build();
        response.addCookie(cookie);
    }
}
