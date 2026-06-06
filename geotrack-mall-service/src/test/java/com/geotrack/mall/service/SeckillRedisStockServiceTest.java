package com.geotrack.mall.service;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SeckillRedisStockServiceTest {

    private final StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
    private final SeckillRedisStockService service = new SeckillRedisStockService(redisTemplate);

    @Test
    void tryDeductOneReturnsLuaResult() {
        when(redisTemplate.execute(
                ArgumentMatchers.<RedisScript<Long>>any(),
                ArgumentMatchers.<List<String>>any()
        )).thenReturn(8L);

        long result = service.tryDeductOne(10L, 20L);

        assertEquals(8L, result);
    }

    @Test
    void tryDeductOneTreatsNullAsNoStock() {
        when(redisTemplate.execute(
                ArgumentMatchers.<RedisScript<Long>>any(),
                ArgumentMatchers.<List<String>>any()
        )).thenReturn(null);

        long result = service.tryDeductOne(10L, 20L);

        assertEquals(SeckillRedisStockService.LUA_NO_STOCK, result);
    }

    @Test
    @SuppressWarnings("unchecked")
    void compensateRestoresStockAndUserMarker() {
        ValueOperations<String, String> ops = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(ops);

        service.compensate(10L, 20L);

        verify(ops).increment(eq("seckill:stock:10"));
        verify(redisTemplate).delete(eq("seckill:user:10:20"));
    }
}
