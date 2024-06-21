package com.GujjuSajang.Jwt.dto;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.*;

import java.util.Date;

import static com.GujjuSajang.Jwt.util.JwtUtil.KEY_ID;
import static com.GujjuSajang.Jwt.util.JwtUtil.KEY_MAIL;
import static javax.management.timer.Timer.ONE_MINUTE;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class TokenUserInfo {

    private Long id;
    private String mail;

    public Claims createClaims(int expiresMin) {
        Claims claims = Jwts.claims();
        Date now = new Date();

        claims.put(KEY_ID, this.id);
        claims.put(KEY_MAIL, this.mail);
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
