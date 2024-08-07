package com.GujjuSajang.cart.repository;

import com.GujjuSajang.core.dto.CartDto;
import com.GujjuSajang.core.exception.ErrorCode;
import com.GujjuSajang.core.exception.RedisException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CartRedisRepository {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String KEY_PREFIX = "cart::";
    private static final int MIN = 10080;

    public void save(Long memberId, CartDto cartDto) {
        try {
            redisTemplate.opsForValue().set(KEY_PREFIX + memberId, cartDto, Duration.ofMinutes(MIN));
        } catch (Exception e) {
            throw new RedisException(ErrorCode.REDIS_OPERATION_FAILURE, e);
        }
    }

    public Optional<CartDto> get(Long memberId) {
        try {
            return Optional.ofNullable((CartDto) redisTemplate.opsForValue().get(KEY_PREFIX + memberId));
        } catch (Exception e) {
            throw new RedisException(ErrorCode.REDIS_OPERATION_FAILURE, e);
        }
    }

}
