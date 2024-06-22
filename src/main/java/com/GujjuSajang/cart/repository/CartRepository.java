package com.GujjuSajang.cart.repository;

import com.GujjuSajang.cart.dto.CartDto;
import com.GujjuSajang.exception.ErrorCode;
import com.GujjuSajang.exception.RedisException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CartRepository {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String KEY_PREFIX = "cart::";

    public void save(Long id, CartDto cartDto) {
        try {
            redisTemplate.opsForValue().set(KEY_PREFIX + id, cartDto);
        } catch (Exception e) {
            throw new RedisException(ErrorCode.REDIS_OPERATION_FAILURE, e);
        }
    }

    public Optional<CartDto> get(Long id) {
        try {
            return Optional.ofNullable((CartDto) redisTemplate.opsForValue().get(KEY_PREFIX + id));
        } catch (Exception e) {
            throw new RedisException(ErrorCode.REDIS_OPERATION_FAILURE, e);
        }
    }

    public void delete(Long id) {
        try {
            redisTemplate.delete(KEY_PREFIX + id);
        } catch (Exception e) {
            throw new RedisException(ErrorCode.REDIS_OPERATION_FAILURE, e);
        }
    }
}
