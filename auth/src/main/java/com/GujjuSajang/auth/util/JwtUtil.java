package com.GujjuSajang.auth.util;

import com.GujjuSajang.core.dto.TokenMemberInfo;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;


@Component
public class JwtUtil {

    public static final String BEARER_PREFIX = "Bearer ";
    public static final String KEY_ID = "id";
    public static final String KEY_MAIL = "mail";
    public static final String KEY_MAIL_VERIFIED = "mailVerified";
    public static final String KEY_MEMBER_ROLE = "memberRole";
    public static final String COOKIE_NAME = "accessToken";

    @Value("${jwt.secretKey.access}")
    private String accessKey;
    @Value("${jwt.secretKey.refresh}")
    private String refreshKey;
    @Getter
    @Value("${jwt.expired-time.access}")
    private int accessTokenExpired;
    @Getter
    @Value("${jwt.expired-time.refresh}")
    private int refreshTokenExpired;
    @Getter
    private Key encodedAccessKey;
    @Getter
    private Key encodedRefreshKey;

    @PostConstruct
    private void init() {
        encodedAccessKey = Keys.hmacShaKeyFor(accessKey.getBytes());
        encodedRefreshKey = Keys.hmacShaKeyFor(refreshKey.getBytes());
    }

    public static TokenMemberInfo getTokenMemberInfo(HttpServletRequest request) {
        return (TokenMemberInfo) request.getAttribute("tokenMemberInfo");
    }

}
