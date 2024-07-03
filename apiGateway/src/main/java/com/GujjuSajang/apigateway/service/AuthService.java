package com.GujjuSajang.apigateway.service;


import com.GujjuSajang.apigateway.dto.RefreshTokenDto;
import com.GujjuSajang.apigateway.dto.TokenInfo;
import com.GujjuSajang.apigateway.dto.TokenMemberInfo;
import com.GujjuSajang.apigateway.exception.ErrorCode;
import com.GujjuSajang.apigateway.exception.MemberException;
import com.GujjuSajang.apigateway.exception.TokenException;
import com.GujjuSajang.apigateway.repository.AuthRedisRepository;
import com.GujjuSajang.apigateway.util.JwtIssuer;
import com.GujjuSajang.apigateway.util.JwtParser;
import com.GujjuSajang.apigateway.util.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.GujjuSajang.apigateway.util.JwtUtil.BEARER_PREFIX;
import static com.GujjuSajang.apigateway.util.JwtUtil.KEY_ID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthRedisRepository authRedisRepository;
    private final JwtParser jwtParser;
    private final JwtIssuer jwtIssuer;
    private final JwtUtil jwtUtil;

    // 토큰 발급
    @Transactional
    public TokenInfo issueTokens(TokenMemberInfo memberInfo) {

        // 엑세스 토큰 발급
        String accessToken = jwtIssuer.issureToken(
                memberInfo.createClaims(jwtUtil.getAccessTokenExpired()),
                jwtUtil.getEncodedAccessKey()
        );

        // 리프레시 토큰 발급
        String refreshToken = jwtIssuer.issureToken(
                memberInfo.createClaims(jwtUtil.getRefreshTokenExpired()),
                jwtUtil.getEncodedRefreshKey()
        );

        // 리프레시 토큰 레디스에 저장
        authRedisRepository.save(memberInfo.getId(), refreshToken, jwtUtil.getRefreshTokenExpired());

        // 토큰 반환
        return TokenInfo.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .prefix(BEARER_PREFIX)
                .build();
    }

    // 토큰 재발급
    @Transactional
    public TokenInfo refreshToken(RefreshTokenDto refreshTokenDto) {
        // 리프레시토큰 파싱
        Claims claims = jwtParser.parseToken(refreshTokenDto.getRefreshToken(), jwtUtil.getEncodedRefreshKey());

        // 레디스에 저장되있는 리프레시토큰 가져오되 없을 경우 에러 던지기
        String getRefreshToken = authRedisRepository.getRefreshToken(claims.get(KEY_ID, Long.class))
                .orElseThrow(() -> new TokenException(ErrorCode.INVALID_TOKEN));

        // 요청시 받은 리프레시토큰과 레디스에서 가져온 리프레시토큰이 같은지 검증
        validateRefreshToken(refreshTokenDto.getRefreshToken(), getRefreshToken);

        TokenMemberInfo memberInfo = parseRefreshToken(refreshTokenDto.getRefreshToken());

        String accessToken = jwtIssuer.issureToken(
                memberInfo.createClaims(jwtUtil.getAccessTokenExpired()),
                jwtUtil.getEncodedAccessKey()
        );

        // 새 토큰 발급
        return TokenInfo.builder()
                .accessToken(accessToken)
                .prefix(BEARER_PREFIX)
                .build();
    }

    // 리프레시 토큰 검증
    private void validateRefreshToken(String refreshToken, String getRefreshToken) {
        if (!refreshToken.equals(getRefreshToken)) {
            throw new TokenException(ErrorCode.INVALID_TOKEN);
        }
    }

    // 토큰 파싱
    public TokenMemberInfo parseAccessToken(String accessToken) {
        return TokenMemberInfo.from(
                jwtParser.parseToken(accessToken, jwtUtil.getEncodedAccessKey()
                )
        );
    }

    public TokenMemberInfo parseRefreshToken(String refreshToken) {
        return TokenMemberInfo.from(
                jwtParser.parseToken(refreshToken, jwtUtil.getEncodedRefreshKey())
        );
    }

}
