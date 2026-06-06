package com.geotrack.common.guard;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.geotrack.common.exception.BizException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.function.Supplier;

public class InterfaceGuardService {

    private static final String FREQ_PREFIX = "guard:freq:";
    private static final String IDEM_PREFIX = "guard:idem:";

    private final StringRedisTemplate redisTemplate;

    public InterfaceGuardService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public <T> T protect(
            String resource,
            double qps,
            Long userId,
            String idempotencyToken,
            Duration tokenTtl,
            Duration userWindow,
            int userMaxRequests,
            Supplier<T> action
    ) {
        SentinelFlowRuleRegistrar.registerQpsRule(resource, qps);
        try (Entry ignored = SphU.entry(resource)) {
            checkUserFrequency(resource, userId, userWindow, userMaxRequests);
            checkIdempotencyToken(resource, userId, idempotencyToken, tokenTtl);
            return action.get();
        } catch (BlockException e) {
            throw new BizException("请求过于频繁，请稍后再试");
        }
    }

    private void checkUserFrequency(String resource, Long userId, Duration window, int maxRequests) {
        if (userId == null || maxRequests <= 0 || window == null || window.isZero() || window.isNegative()) {
            return;
        }
        long bucket = System.currentTimeMillis() / window.toMillis();
        String key = FREQ_PREFIX + resource + ":" + userId + ":" + bucket;
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1L) {
            redisTemplate.expire(key, window.plusSeconds(1));
        }
        if (count != null && count > maxRequests) {
            throw new BizException("操作太频繁，请稍后再试");
        }
    }

    private void checkIdempotencyToken(String resource, Long userId, String token, Duration ttl) {
        if (!StringUtils.hasText(token)) {
            throw new BizException("请携带幂等 Token：X-Idempotency-Key");
        }
        String key = IDEM_PREFIX + resource + ":" + userId + ":" + token;
        Boolean ok = redisTemplate.opsForValue().setIfAbsent(key, "1", ttl);
        if (!Boolean.TRUE.equals(ok)) {
            throw new BizException("重复请求已拦截，请勿重复提交");
        }
    }
}
