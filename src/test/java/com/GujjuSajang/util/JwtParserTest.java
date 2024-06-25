package com.GujjuSajang.util;

import com.GujjuSajang.Jwt.util.JwtParser;
import com.GujjuSajang.exception.ErrorCode;
import com.GujjuSajang.exception.TokenException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.Key;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
public class JwtParserTest {

    private final JwtParser jwtParser = new JwtParser();
    private final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    private final String validToken = Jwts.builder().setSubject("test").signWith(key).compact();

    @DisplayName("유효한 토큰 파싱 성공")
    @Test
    void parseToken_Success() {
        // when
        Claims claims = jwtParser.parseToken(validToken, key);

        // then
        assertThat(claims).isNotNull();
        assertThat(claims.getSubject()).isEqualTo("test");
    }

    @DisplayName("만료된 토큰 파싱 실패")
    @Test
    void parseToken_ExpiredJwtException() {
        // given
        String expiredToken = Jwts.builder()
                .setSubject("test")
                .setExpiration(new java.util.Date(System.currentTimeMillis() - 1000))
                .signWith(key)
                .compact();

        // when & then
        assertThatThrownBy(() -> jwtParser.parseToken(expiredToken, key))
                .isInstanceOf(TokenException.class)
                .hasMessageContaining(ErrorCode.TOKEN_EXPIRED.getErrorMessage());
    }

    @DisplayName("잘못된 형식의 토큰 파싱 실패")
    @Test
    void parseToken_MalformedJwtException() {
        // given

        // when

        // then
        assertThatThrownBy(() -> jwtParser.parseToken("token", key))
                .isInstanceOf(TokenException.class)
                .hasMessageContaining(ErrorCode.MALFORMED_TOKEN.getErrorMessage());
    }

    @DisplayName("서명이 잘못된 토큰 파싱 실패")
    @Test
    void parseToken_SecurityException() {
        // given
        Key anotherKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        String tokenWithWrongSignature = Jwts.builder().setSubject("test").signWith(anotherKey).compact();

        // when

        // then
        assertThatThrownBy(() -> jwtParser.parseToken(tokenWithWrongSignature, key))
                .isInstanceOf(TokenException.class)
                .hasMessageContaining(ErrorCode.INVALID_TOKEN.getErrorMessage());
    }

}
