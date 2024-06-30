package com.GujjuSajang.apigateway.dto;


import com.GujjuSajang.apigateway.type.MemberRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Date;

import static com.GujjuSajang.apigateway.util.JwtUtil.*;
import static javax.management.timer.Timer.ONE_MINUTE;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class TokenMemberInfo {

    private Long id;
    private String mail;
    private boolean mailVerified;
    private MemberRole role;

    public Claims createClaims(int expiresMin) {
        Claims claims = Jwts.claims();
        Date now = new Date();

        claims.put(KEY_ID, this.id);
        claims.put(KEY_MAIL, this.mail);
        claims.put(KEY_MAIL_VERIFIED, this.mailVerified);
        claims.put(KEY_MEMBER_ROLE, this.role);
        claims.setIssuedAt(now);
        claims.setExpiration(new Date(now.getTime() + expiresMin * ONE_MINUTE));

        return claims;
    }

    public static TokenMemberInfo from(Claims claims) {
        return TokenMemberInfo.builder()
                .id(claims.get(KEY_ID, Long.class))
                .mail(claims.get(KEY_MAIL, String.class))
                .mailVerified(claims.get(KEY_MAIL_VERIFIED, Boolean.class))
                .role(MemberRole.valueOf(claims.get(KEY_MEMBER_ROLE, String.class)))
                .build();
    }

}
