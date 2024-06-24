package com.GujjuSajang.Jwt.Repository;

import com.GujjuSajang.exception.ErrorCode;
import com.GujjuSajang.exception.RedisException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRedisRepository {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String KEY_PREFIX = "refreshToken::";

    public void save(long id, String refreshToken, int expiresMin) {
        try {
            redisTemplate.opsForValue().set(KEY_PREFIX + id, refreshToken, Duration.ofMinutes(expiresMin));
        } catch (Exception e) {
            throw new RedisException(ErrorCode.REDIS_OPERATION_FAILURE, e);
        }
    }

    public Optional<String> getRefreshToken(long id) {
        try {
            String token = (String) redisTemplate.opsForValue().get(KEY_PREFIX + id);
            return Optional.ofNullable(token);
        } catch (Exception e) {
            throw new RedisException(ErrorCode.REDIS_OPERATION_FAILURE, e);
        }
    }

    public void delete(long id) {
        try {
            redisTemplate.delete(KEY_PREFIX + id);
        } catch (Exception e) {
            throw new RedisException(ErrorCode.REDIS_OPERATION_FAILURE, e);
        }
    }

}
