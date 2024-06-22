package com.GujjuSajang.Jwt.service;

import com.GujjuSajang.Jwt.dto.TokenInfo;
import com.GujjuSajang.Jwt.dto.TokenMemberInfo;
import com.GujjuSajang.Jwt.util.JwtIssuer;
import com.GujjuSajang.Jwt.util.JwtParser;
import com.GujjuSajang.Jwt.util.JwtUtil;
import com.GujjuSajang.exception.ErrorCode;
import com.GujjuSajang.exception.TokenException;
import com.GujjuSajang.redis.repository.RefreshTokenRepository;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.GujjuSajang.Jwt.util.JwtUtil.BEARER_PREFIX;
import static com.GujjuSajang.Jwt.util.JwtUtil.KEY_ID;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtParser jwtParser;
    private final JwtIssuer jwtIssuer;
    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;

    // 토큰 발급
    @Transactional
    public TokenInfo issueTokens(TokenMemberInfo userInfo) {

        // 엑세스 토큰 발급
        String accessToken = jwtIssuer.issureToken(
                userInfo.createClaims(jwtUtil.getAccessTokenExpired()),
                jwtUtil.getEncodedAccessKey()
        );

        // 리프레시 토큰 발급
        String refreshToken = jwtIssuer.issureToken(
                userInfo.createClaims(jwtUtil.getRefreshTokenExpired()),
                jwtUtil.getEncodedRefreshKey()
        );

        // 리프레시 토큰 레디스에 저장
        refreshTokenRepository.save(userInfo.getId(), refreshToken, jwtUtil.getRefreshTokenExpired());

        // 토큰 반환
        return TokenInfo.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .prefix(BEARER_PREFIX)
                .build();
    }

    // 토큰 재발급
    @Transactional
    public TokenInfo refreshToken(String refreshToken) {
        // 리프레시토큰 파싱
        Claims claims = jwtParser.parseToken(refreshToken, jwtUtil.getEncodedRefreshKey());

        // 레디스에 저장되있는 리프레시토큰 가져오되 없을 경우 에러 던지기
        String getRefreshToken = refreshTokenRepository.getRefreshToken(claims.get(KEY_ID, Long.class))
                .orElseThrow(() -> new TokenException(ErrorCode.INVALID_TOKEN));

        // 요청시 받은 리프레시토큰과 레디스에서 가져온 리프레시토큰이 같은지 검증
        validateRefreshToken(refreshToken, getRefreshToken);

        // 새 토큰 발급
        return issueTokens(TokenMemberInfo.from(claims));
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


}
