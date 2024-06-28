package com.GujjuSajang.auth.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Component;

import java.security.Key;

@Component
public class JwtIssuer {

    public String issureToken(Claims claims, Key key) {
        return Jwts.builder()
                .setClaims(claims)
                .signWith(key)
                .compact();
    }

}
