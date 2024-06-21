package com.GujjuSajang.redis.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRepository {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String KEY_PREFIX = "refreshToken::";

    public void save(long id, String refreshToken, int expiresMin) {
        redisTemplate.opsForValue().set(KEY_PREFIX + id, refreshToken, Duration.ofMinutes(expiresMin));
    }

    public Optional<String> getRefreshToken(long id) {
        String token = (String) redisTemplate.opsForValue().get(KEY_PREFIX + id);
        return Optional.ofNullable(token);
    }

    public void delete(long id) {
        redisTemplate.delete(KEY_PREFIX + id);
    }

}
