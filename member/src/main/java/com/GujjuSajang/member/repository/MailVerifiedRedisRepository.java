package com.GujjuSajang.member.repository;

import com.GujjuSajang.core.exception.ErrorCode;
import com.GujjuSajang.core.exception.RedisException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MailVerifiedRedisRepository {

    private static final String MAIL_VERIFIED_PREFIX = "MAIL_VERIFIED::";

    private final RedisTemplate<String, Object> redisTemplate;

    public void save(Long id, String code) {

        try {
            redisTemplate.opsForValue().set(MAIL_VERIFIED_PREFIX + id, code, Duration.ofMinutes(5));
        } catch (Exception e) {
            e.printStackTrace();
            throw new RedisException(ErrorCode.REDIS_OPERATION_FAILURE, e);
        }
    }

    public Optional<String> getCode(Long id) {
        try {
            String code = (String) redisTemplate.opsForValue().get(MAIL_VERIFIED_PREFIX + id);
            return Optional.ofNullable(code);
        } catch (Exception e) {
            throw new RedisException(ErrorCode.REDIS_OPERATION_FAILURE, e);
        }
    }

    public void delete(Long id) {
        try {
            redisTemplate.delete(MAIL_VERIFIED_PREFIX + id);
        } catch (Exception e) {
            throw new RedisException(ErrorCode.REDIS_OPERATION_FAILURE, e);
        }
    }

}
