package com.GujjuSajang.redis.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MailVerifiedRepository {

    private static final String MAIL_VERIFIED_PREFIX = "MAIL_VERIFIED::";

    private final RedisTemplate<String, Object> redisTemplate;

    public void save(long id, String code) {
        redisTemplate.opsForValue().set(MAIL_VERIFIED_PREFIX + id, code, Duration.ofMinutes(5));
    }

    public Optional<String> getCode(long id) {
        String code = (String) redisTemplate.opsForValue().get(MAIL_VERIFIED_PREFIX + id);
        return Optional.ofNullable(code);
    }

    public void delete(long id) {
        redisTemplate.delete(MAIL_VERIFIED_PREFIX + id);
    }



}
