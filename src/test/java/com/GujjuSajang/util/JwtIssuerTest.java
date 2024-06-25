package com.GujjuSajang.util;

import com.GujjuSajang.Jwt.util.JwtIssuer;
import com.GujjuSajang.member.type.MemberRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.impl.DefaultClaims;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.Key;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JwtIssuerTest {

    @InjectMocks
    private JwtIssuer jwtIssuer;

    private Claims claims;
    private Key key;

    @BeforeEach
    void setUp() {
        claims = new DefaultClaims();
        claims.setSubject("subject");
        claims.put("role", "MEMBER");

        key = Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS256);
    }

    @DisplayName("JWT 토큰 생성 성공")
    @Test
    void issueToken_Success() {
        // given
        // when
        String token = jwtIssuer.issureToken(claims, key);

        // then
        assertThat(token).isNotNull();
        Claims parsedClaims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        assertThat(parsedClaims.getSubject()).isEqualTo("subject");
        assertThat(parsedClaims.get("role")).isEqualTo("MEMBER");
    }

}
