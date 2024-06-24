package com.GujjuSajang.Jwt.util;

import com.GujjuSajang.Jwt.dto.TokenMemberInfo;
import com.GujjuSajang.Jwt.service.JwtService;
import com.GujjuSajang.exception.ErrorCode;
import com.GujjuSajang.exception.MemberException;
import com.GujjuSajang.exception.TokenException;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;

import static com.GujjuSajang.Jwt.util.JwtUtil.BEARER_PREFIX;

@Component
@RequiredArgsConstructor
public class JwtFilter implements Filter {

    private final JwtService jwtService;

    // 헤더에서 토큰 가져오기
    private String getJwtFrom(HttpServletRequest request) {
        String bearerToken = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }

    // 토큰
    private boolean authenticateToken(HttpServletRequest request, String token) {
        if (StringUtils.hasText(token)) {
            TokenMemberInfo tokenMemberInfo = jwtService.parseAccessToken(token);
            request.setAttribute("tokenMemberInfo", tokenMemberInfo);
            return tokenMemberInfo.isMailVerified();
        }
        return false;
    }

    private void mailVerify(boolean isMailVerified) {
        if (!isMailVerified) {
            throw new MemberException(ErrorCode.MAIL_NOT_VERIFIED);
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        try {
            mailVerify(authenticateToken(request, getJwtFrom(request)));
            filterChain.doFilter(request, response);
        } catch (TokenException e) {
            response.sendError(e.getStatus(), e.getMessage());
        }

    }

    @Override
    public void destroy() {
        Filter.super.destroy();
    }
}
