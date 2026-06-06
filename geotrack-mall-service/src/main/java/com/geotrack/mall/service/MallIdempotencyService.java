package com.geotrack.mall.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * 兑换/秒杀幂等：短 TTL 锁防并发双单；成功后缓存 orderNo 便于客户端重试返回同一结果（需求 4.7.2）。
 */
@Service
public class MallIdempotencyService {

    private static final String LOCK_PREFIX = "mall:idem:lock:";
    private static final String RESULT_PREFIX = "mall:idem:result:";

    private final StringRedisTemplate redisTemplate;

    public MallIdempotencyService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean tryBegin(Long userId, String idempotencyKey) {
        String lockKey = LOCK_PREFIX + userId + ":" + idempotencyKey;
        Boolean ok = redisTemplate.opsForValue().setIfAbsent(lockKey, "1", Duration.ofSeconds(90));
        return Boolean.TRUE.equals(ok);
    }

    public void end(Long userId, String idempotencyKey) {
        redisTemplate.delete(LOCK_PREFIX + userId + ":" + idempotencyKey);
    }

    public String getCachedOrderNo(Long userId, String idempotencyKey) {
        return redisTemplate.opsForValue().get(RESULT_PREFIX + userId + ":" + idempotencyKey);
    }

    public void cacheOrderNo(Long userId, String idempotencyKey, String orderNo) {
        redisTemplate.opsForValue().set(RESULT_PREFIX + userId + ":" + idempotencyKey, orderNo, Duration.ofDays(1));
    }
}
