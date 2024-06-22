package com.GujjuSajang.Jwt.dto;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.*;

import java.util.Date;

import static com.GujjuSajang.Jwt.util.JwtUtil.*;
import static javax.management.timer.Timer.ONE_MINUTE;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class TokenUserInfo {

    private Long id;
    private String mail;
    private boolean mailVerified;

    public Claims createClaims(int expiresMin) {
        Claims claims = Jwts.claims();
        Date now = new Date();

        claims.put(KEY_ID, this.id);
        claims.put(KEY_MAIL, this.mail);
        claims.put(KEY_MAIL_VERIFIED, this.mailVerified);
        claims.setIssuedAt(now);
        claims.setExpiration(new Date(now.getTime() + expiresMin * ONE_MINUTE));

        return claims;
    }

    public static TokenUserInfo from(Claims claims) {
        return TokenUserInfo.builder()
                .id(claims.get(KEY_ID, Long.class))
                .mail(claims.get(KEY_MAIL, String.class))
                .build();
    }

}
