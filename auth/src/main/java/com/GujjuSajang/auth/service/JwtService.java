package com.GujjuSajang.auth.service;


import com.GujjuSajang.auth.Repository.RefreshTokenRedisRepository;
import com.GujjuSajang.auth.dto.TokenMemberInfo;
import com.GujjuSajang.auth.util.JwtIssuer;
import com.GujjuSajang.auth.util.JwtParser;
import com.GujjuSajang.auth.util.JwtUtil;
import com.GujjuSajang.core.dto.TokenInfo;
import com.GujjuSajang.core.exception.ErrorCode;
import com.GujjuSajang.core.exception.TokenException;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.GujjuSajang.auth.util.JwtUtil.BEARER_PREFIX;
import static com.GujjuSajang.auth.util.JwtUtil.KEY_ID;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtParser jwtParser;
    private final JwtIssuer jwtIssuer;
    private final JwtUtil jwtUtil;
    private final RefreshTokenRedisRepository refreshTokenRedisRepository;

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
//        refreshTokenRedisRepository.save(userInfo.getId(), refreshToken, jwtUtil.getRefreshTokenExpired());

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
//        String getRefreshToken = refreshTokenRedisRepository.getRefreshToken(claims.get(KEY_ID, Long.class))
//                .orElseThrow(() -> new TokenException(ErrorCode.INVALID_TOKEN));

        // 요청시 받은 리프레시토큰과 레디스에서 가져온 리프레시토큰이 같은지 검증
//        validateRefreshToken(refreshToken, getRefreshToken);

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
