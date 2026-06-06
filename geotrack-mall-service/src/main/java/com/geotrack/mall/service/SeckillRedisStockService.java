package com.geotrack.mall.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 秒杀防超卖核心实现：通过单个 Lua 脚本在 Redis 内完成限购校验、库存校验、
 * 扣减库存和写入用户购买标记，避免并发场景下先读后写造成超卖。
 */
@Service
public class SeckillRedisStockService {

    public static final long LUA_NO_STOCK = -1L;
    public static final long LUA_ALREADY_BOUGHT = -2L;

    private static final String STOCK_KEY_PREFIX = "seckill:stock:";
    private static final String USER_KEY_PREFIX = "seckill:user:";

    // 原子执行：限购校验 -> 库存校验 -> 扣减库存 -> 写入购买标记。
    private static final String SECKILL_LUA = """
            if redis.call('EXISTS', KEYS[2]) == 1 then
              return -2
            end
            local stock = tonumber(redis.call('GET', KEYS[1]) or '0')
            if stock < 1 then
              return -1
            end
            redis.call('DECR', KEYS[1])
            redis.call('SET', KEYS[2], '1')
            return tonumber(redis.call('GET', KEYS[1]))
            """;

    private final StringRedisTemplate redisTemplate;
    private final DefaultRedisScript<Long> seckillScript;

    public SeckillRedisStockService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.seckillScript = new DefaultRedisScript<>();
        this.seckillScript.setResultType(Long.class);
        this.seckillScript.setScriptText(SECKILL_LUA);
    }

    /** 首次活动时把 DB 库存灌入 Redis；已存在则不覆盖，避免运营改库后和缓存打架。 */
    public void warmStockIfAbsent(Long goodsId, int dbStock) {
        String key = STOCK_KEY_PREFIX + goodsId;
        Boolean set = redisTemplate.opsForValue().setIfAbsent(key, String.valueOf(Math.max(dbStock, 0)));
        if (Boolean.TRUE.equals(set)) {
            return;
        }
        String v = redisTemplate.opsForValue().get(key);
        if (v == null) {
            redisTemplate.opsForValue().set(key, String.valueOf(Math.max(dbStock, 0)));
        }
    }

    /**
     * @return 扣减后的剩余库存；{@link #LUA_NO_STOCK} 表示无库存；{@link #LUA_ALREADY_BOUGHT} 表示已买过
     */
    public long tryDeductOne(Long goodsId, Long userId) {
        String stockKey = STOCK_KEY_PREFIX + goodsId;
        String userKey = USER_KEY_PREFIX + goodsId + ":" + userId;
        Long r = redisTemplate.execute(seckillScript, List.of(stockKey, userKey));
        return r == null ? LUA_NO_STOCK : r;
    }

    /** 扣积分失败时回滚 Redis 库存和限购标记。 */
    public void compensate(Long goodsId, Long userId) {
        String stockKey = STOCK_KEY_PREFIX + goodsId;
        String userKey = USER_KEY_PREFIX + goodsId + ":" + userId;
        redisTemplate.opsForValue().increment(stockKey);
        redisTemplate.delete(userKey);
    }
}
