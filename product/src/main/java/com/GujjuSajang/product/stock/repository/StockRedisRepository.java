package com.GujjuSajang.product.stock.repository;


import com.GujjuSajang.core.exception.ErrorCode;
import com.GujjuSajang.core.exception.RedisException;
import com.GujjuSajang.product.stock.dto.StockDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class StockRedisRepository {

    private static final String KEY_PREFIX = "stock::";
    private final RedisTemplate<String, Object> redisTemplate;
    private static final int TTL_MIN = 10;
    private static final String SAVE_ALL_SCRIPT =
            "local ttl_min = 5 " +
                    "for i = 1, #KEYS do " +
                    "redis.call('SET', KEYS[i], ARGV[i]) " +
                    "redis.call('EXPIRE', KEYS[i], ttl_min * 60) " +
                    "end " +
                    "return true";
    private static final String GET_ALL_SCRIPT =
            "local result = {} " +
                    "for i = 1, #KEYS do " +
                    "local value = redis.call('GET', KEYS[i]) " +
                    "if value then " +
                    "result[i] = value " +
                    "end " +
                    "end " +
                    "return result";
    private final RedisScript<Boolean> saveAllScript = new DefaultRedisScript<>(SAVE_ALL_SCRIPT, Boolean.class);
    private final RedisScript<List> getAllByProductIdsScript = new DefaultRedisScript<>(GET_ALL_SCRIPT, List.class);

    public void save(StockDto stockDto) {
        try {
            redisTemplate.opsForValue().set(KEY_PREFIX + stockDto.getProductId(), stockDto, Duration.ofSeconds(TTL_MIN));
        } catch (Exception e) {
            throw new RedisException(ErrorCode.REDIS_OPERATION_FAILURE, e);
        }
    }

    public Optional<StockDto> get(Long productId) {
        try {
            return Optional.ofNullable((StockDto) redisTemplate.opsForValue().get(KEY_PREFIX + productId));
        } catch (Exception e) {
            throw new RedisException(ErrorCode.REDIS_OPERATION_FAILURE, e);
        }
    }

    public void saveAll(List<StockDto> stockDto) {
        List<String> keys = stockDto.stream()
                .map(stock -> KEY_PREFIX + stock.getProductId())
                .collect(Collectors.toList());
        List<StockDto> values = new ArrayList<>(stockDto);
        try {
            redisTemplate.execute(saveAllScript, keys, values.toArray(new Object[0]));
        } catch (Exception e) {
            throw new RedisException(ErrorCode.REDIS_OPERATION_FAILURE, e);
        }
    }

    public List<StockDto> getAllByProductIds(List<Long> productIds) {
        List<String> keys = productIds.stream()
                .map(id -> KEY_PREFIX + id)
                .collect(Collectors.toList());

        try {
            List<Object> results = redisTemplate.execute(getAllByProductIdsScript, keys);
            return Objects.requireNonNull(results).stream()
                    .filter(Objects::nonNull)
                    .map(value -> (StockDto) value)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RedisException(ErrorCode.REDIS_OPERATION_FAILURE, e);
        }

    }


}
