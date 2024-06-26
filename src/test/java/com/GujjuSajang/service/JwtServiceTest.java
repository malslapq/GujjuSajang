package com.GujjuSajang.service;

import com.GujjuSajang.Jwt.Repository.RefreshTokenRedisRepository;
import com.GujjuSajang.Jwt.dto.TokenInfo;
import com.GujjuSajang.Jwt.dto.TokenMemberInfo;
import com.GujjuSajang.Jwt.service.JwtService;
import com.GujjuSajang.Jwt.util.JwtIssuer;
import com.GujjuSajang.Jwt.util.JwtParser;
import com.GujjuSajang.Jwt.util.JwtUtil;
import com.GujjuSajang.exception.ErrorCode;
import com.GujjuSajang.exception.TokenException;
import com.GujjuSajang.member.type.MemberRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.impl.DefaultClaims;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.Key;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class JwtServiceTest {

    @Mock
    private JwtParser jwtParser;
    @Mock
    private JwtIssuer jwtIssuer;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private RefreshTokenRedisRepository refreshTokenRedisRepository;
    @InjectMocks
    private JwtService jwtService;

    TokenMemberInfo tokenMemberInfo;
    TokenInfo tokenInfo;
    Claims claims;
    private static final Key ENCODED_REFRESH_KEY = Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS256);

    @BeforeEach
    void init() {
        tokenMemberInfo = TokenMemberInfo.builder()
                .id(1L)
                .mail("mail")
                .role(MemberRole.MEMBER)
                .mailVerified(true)
                .build();
        tokenInfo = TokenInfo.builder()
                .accessToken("newAccessToken")
                .refreshToken("newRefreshToken")
                .prefix("Bearer ")
                .build();
        claims = new DefaultClaims();
        claims.put("id", 1L);
        claims.put("mail", "mail");
        claims.put("mailVerified", true);  // Boolean 값으로 설정
        claims.put("memberRole", MemberRole.MEMBER.name());
    }

    @DisplayName("토큰 발급 성공")
    @Test
    void token_Issue_Success() {
        // given
        given(jwtIssuer.issureToken(any(), any()))
                .willReturn(tokenInfo.getAccessToken())
                .willReturn(tokenInfo.getRefreshToken());

        // when
        TokenInfo result = jwtService.issueTokens(tokenMemberInfo);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getAccessToken()).isEqualTo(tokenInfo.getAccessToken());
        assertThat(result.getRefreshToken()).isEqualTo(tokenInfo.getRefreshToken());
        assertThat(result.getPrefix()).isEqualTo(tokenInfo.getPrefix());
    }

    @DisplayName("토큰 발급 실패 -  회원 정보 없음")
    @Test
    void fail() {
        // given

        // when

        // then
        assertThatThrownBy(() -> jwtService.issueTokens(null))
                .isInstanceOf(NullPointerException.class);
    }

    @DisplayName("토큰 재발급 성공")
    @Test
    void refreshTokens() {
        //given
        given(jwtParser.parseToken(any(), any()))
                .willReturn(claims);
        given(refreshTokenRedisRepository.getRefreshToken(anyLong()))
                .willReturn(Optional.of("refreshToken"));
        given(jwtIssuer.issureToken(any(), any())).willReturn(tokenInfo.getAccessToken(), tokenInfo.getRefreshToken());

        //when
        TokenInfo result = jwtService.refreshToken("refreshToken");

        //then
        assertThat(result.getAccessToken()).isEqualTo(tokenInfo.getAccessToken());
        assertThat(result.getRefreshToken()).isEqualTo(tokenInfo.getRefreshToken());
        assertThat(result.getPrefix()).isEqualTo(tokenInfo.getPrefix());
    }

    @DisplayName("토큰 재발급 실패 - 리프레시 토큰 없음")
    @Test
    void tokenRefresh_Fail_TokenNotFound() {
        // given
        given(jwtUtil.getEncodedRefreshKey()).willReturn(ENCODED_REFRESH_KEY);
        given(jwtParser.parseToken(anyString(), any(Key.class))).willReturn(claims);
        given(refreshTokenRedisRepository.getRefreshToken(1L)).willReturn(Optional.empty());

        // when

        // then
        assertThatThrownBy(() -> jwtService.refreshToken("accessToken"))
                .isInstanceOf(TokenException.class)
                .hasMessageContaining(ErrorCode.INVALID_TOKEN.getErrorMessage());
    }

    @DisplayName("토큰 재발급 실패 - 리프레시 토큰 불일치")
    @Test
    void tokenRefresh_Fail_TokenMismatch() {
        // given
        given(jwtUtil.getEncodedRefreshKey()).willReturn(ENCODED_REFRESH_KEY);
        given(jwtParser.parseToken(anyString(), any(Key.class))).willReturn(claims);
        given(refreshTokenRedisRepository.getRefreshToken(1L)).willReturn(Optional.of(tokenInfo.getAccessToken()));

        // when

        // then
        assertThatThrownBy(() -> jwtService.refreshToken(tokenInfo.getRefreshToken()))
                .isInstanceOf(TokenException.class)
                .hasMessageContaining(ErrorCode.INVALID_TOKEN.getErrorMessage());
    }

}
