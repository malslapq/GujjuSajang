package com.GujjuSajang.apigateway.filter;

import com.GujjuSajang.apigateway.dto.TokenMemberInfo;
import com.GujjuSajang.apigateway.exception.ErrorCode;
import com.GujjuSajang.apigateway.exception.MemberException;
import com.GujjuSajang.apigateway.exception.TokenException;
import com.GujjuSajang.apigateway.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;

@Order(1)
@Component
@RequiredArgsConstructor
public class JwtFilter implements WebFilter {

    public static final String LOGOUT_PATH = "/logout";
    public static final String TOKEN_PATH = "/refresh";
    public static final String LOGIN_PATH = "/member/login";
    public static final String SIGNUP_PATH = "/member/signup";
    public static final String MAIL_VERIFIED_PATH = "/member/mail-verified";
    private static final String BEARER_PREFIX = "Bearer ";
    private final AuthService authService;
    private final ObjectMapper objectMapper;


    private static final List<String> EXCLUDED_PATHS = List.of(
            LOGIN_PATH,
            SIGNUP_PATH,
            TOKEN_PATH,
            MAIL_VERIFIED_PATH,
            LOGOUT_PATH
    );

    // dodilter와 같은 부분 근데 Mono는 비동기 처리임
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        // 로그인시에는 검증 안함
        String requestURI = exchange.getRequest().getPath().toString();
        System.out.println(requestURI);
        if (isExcludedPath(requestURI)) {
            return chain.filter(exchange);
        }

        String token = getJwtFrom(exchange);
        TokenMemberInfo tokenMemberInfo = authenticateToken(token); // 토큰을 인증하고 TokenMemberInfo 객체를 반환

        // 이메일 인증 확인
        mailVerify(tokenMemberInfo.isMailVerified());
        try {
            String tokenMemberInfoJson = objectMapper.writeValueAsString(tokenMemberInfo);
            exchange = exchange.mutate()
                    .request(exchange.getRequest().mutate()
                            .header("tokenMemberInfo", tokenMemberInfoJson)
                            .build())
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("파싱 실패", e);
        }
        // 라우팅 필터에서 쓸 수 있도록 저장
        exchange.getAttributes().put("tokenMemberInfo", tokenMemberInfo);

        // 다음 필터로
        return chain.filter(exchange);
    }

    // 헤더에서 토큰 가져오기
    private String getJwtFrom(ServerWebExchange exchange) {
        String bearerToken = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (!StringUtils.hasText(bearerToken)) {
            throw new TokenException(ErrorCode.MISSING_AUTHORIZATION_HEADER);
        }
        if (!bearerToken.startsWith(BEARER_PREFIX)) {
            System.out.println(bearerToken);
            throw new TokenException(ErrorCode.MALFORMED_TOKEN);
        }
        return bearerToken.substring(BEARER_PREFIX.length());
    }

    // 토큰 인증
    private TokenMemberInfo authenticateToken(String token) {
        return authService.parseAccessToken(token);
    }

    // 메일 인증 검증
    private void mailVerify(boolean isMailVerified) {
        if (!isMailVerified) {
            throw new MemberException(ErrorCode.MAIL_NOT_VERIFIED);
        }
    }

    // 헤더 검증 필요 없는지 체크
    private boolean isExcludedPath(String path) {
        return EXCLUDED_PATHS.stream().anyMatch(path::startsWith);
    }

}