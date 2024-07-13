package com.GujjuSajang.apigateway.controller;

import com.GujjuSajang.apigateway.dto.RefreshTokenDto;
import com.GujjuSajang.apigateway.dto.TokenInfo;
import com.GujjuSajang.apigateway.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    public Mono<ServerResponse> refreshAccessToken(ServerRequest request) {
        return request.bodyToMono(RefreshTokenDto.class)
                .flatMap(refreshTokenDto -> {
                    TokenInfo tokenInfo = authService.refreshToken(refreshTokenDto);

                    // 쿠키 설정
                    ResponseCookie cookie = ResponseCookie.from("accessToken", tokenInfo.getAccessToken())
                            .path("/")
                            .httpOnly(true)
                            .secure(true)
                            .build();

                    return ServerResponse.ok()
                            .cookie(cookie)
                            .bodyValue(tokenInfo);
                });
    }

    public Mono<ServerResponse> logout(ServerRequest request) {
        return request.bodyToMono(RefreshTokenDto.class)
                .flatMap(refreshTokenDto -> {
                    authService.logout(refreshTokenDto);

                    // 쿠키 삭제
                    ResponseCookie deleteCookie = ResponseCookie.from("accessToken", "")
                            .path("/")
                            .httpOnly(true)
                            .secure(true)
                            .maxAge(0)
                            .build();

                    return ServerResponse.ok()
                            .cookie(deleteCookie)
                            .bodyValue("로그아웃 성공");
                });
    }

}
