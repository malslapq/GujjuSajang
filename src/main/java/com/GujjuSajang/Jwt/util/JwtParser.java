package com.GujjuSajang.Jwt.util;

import com.GujjuSajang.exception.ErrorCode;
import com.GujjuSajang.exception.TokenException;
import io.jsonwebtoken.*;
import org.springframework.stereotype.Component;

import java.security.Key;

@Component
public class JwtParser {

    public Claims parseToken(String token, Key key) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            // 만료
            throw new TokenException(ErrorCode.TOKEN_EXPIRED, e);
        } catch (UnsupportedJwtException e) {
            // 지원되지 않는 토큰 형식
            throw new TokenException(ErrorCode.UNSUPPORTED_TOKEN, e);
        } catch (MalformedJwtException e) {
            // 잘못된 형식의 토큰
            throw new TokenException(ErrorCode.MALFORMED_TOKEN, e);
        } catch (SecurityException e) {
            // 잘못된 토큰 서명
            throw new TokenException(ErrorCode.INVALID_SIGNATURE, e);
        } catch (JwtException e) {
            throw new TokenException(ErrorCode.INVALID_TOKEN, e);
        }
    }
}
