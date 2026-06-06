package com.geotrack.mall.service;

import com.geotrack.common.exception.BizException;
import com.geotrack.common.mq.RocketMqTopics;
import com.geotrack.common.mq.SeckillOrderCreateMessage;
import com.geotrack.mall.dto.OrderResultDto;
import com.geotrack.mall.entity.GoodsEntity;
import com.geotrack.mall.entity.OrderEntity;
import com.geotrack.mall.mapper.GoodsMapper;
import com.geotrack.mall.util.OrderNoGenerator;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 秒杀入口只做资格校验、Redis Lua 预扣和消息投递；订单落库、积分扣减、流水记录由 RocketMQ 异步削峰处理。
 */
@Service
public class SeckillOrderService {

    private final GoodsMapper goodsMapper;
    private final SeckillRedisStockService seckillRedisStockService;
    private final MallOrderTxnService mallOrderTxnService;
    private final MallIdempotencyService mallIdempotencyService;
    private final RocketMQTemplate rocketMQTemplate;

    public SeckillOrderService(
            GoodsMapper goodsMapper,
            SeckillRedisStockService seckillRedisStockService,
            MallOrderTxnService mallOrderTxnService,
            MallIdempotencyService mallIdempotencyService,
            RocketMQTemplate rocketMQTemplate
    ) {
        this.goodsMapper = goodsMapper;
        this.seckillRedisStockService = seckillRedisStockService;
        this.mallOrderTxnService = mallOrderTxnService;
        this.mallIdempotencyService = mallIdempotencyService;
        this.rocketMQTemplate = rocketMQTemplate;
    }

    public OrderResultDto place(Long userId, Long goodsId, String idempotencyKey) {
        if (userId == null || goodsId == null) {
            throw new BizException("参数不完整");
        }
        if (!StringUtils.hasText(idempotencyKey)) {
            throw new BizException("请携带幂等键 X-Idempotency-Key");
        }
        String cached = mallIdempotencyService.getCachedOrderNo(userId, idempotencyKey);
        if (cached != null) {
            OrderEntity existed = mallOrderTxnService.findByOrderNo(cached);
            return new OrderResultDto(cached, existed == null ? OrderEntity.STATUS_PENDING : existed.getStatus());
        }
        if (!mallIdempotencyService.tryBegin(userId, idempotencyKey)) {
            throw new BizException("请求处理中，请稍后再试");
        }
        try {
            GoodsEntity g = goodsMapper.selectById(goodsId);
            if (g == null || g.getStatus() == null || g.getStatus() != 1) {
                throw new BizException("商品不存在或已下架");
            }
            if (g.getIsSeckill() == null || g.getIsSeckill() == 0) {
                throw new BizException("该商品不支持秒杀");
            }
            assertInSeckillWindow(g);

            int dbStock = g.getStock() == null ? 0 : g.getStock();
            seckillRedisStockService.warmStockIfAbsent(goodsId, dbStock);

            // 并发下仅 Redis Lua 结果为准；通过后再写库，避免仅 DB 行锁扛瞬时流量
            long lua = seckillRedisStockService.tryDeductOne(goodsId, userId);
            if (lua == SeckillRedisStockService.LUA_ALREADY_BOUGHT) {
                throw new BizException("您已参与过该秒杀活动");
            }
            if (lua == SeckillRedisStockService.LUA_NO_STOCK) {
                throw new BizException("秒杀库存不足");
            }

            String orderNo = OrderNoGenerator.next();
            SeckillOrderCreateMessage message = new SeckillOrderCreateMessage(
                    orderNo,
                    orderNo,
                    userId,
                    goodsId,
                    idempotencyKey
            );
            try {
                rocketMQTemplate.convertAndSend(RocketMqTopics.SECKILL_ORDER_CREATE_TOPIC, message);
            } catch (Exception ex) {
                seckillRedisStockService.compensate(goodsId, userId);
                throw new BizException("秒杀请求排队失败，请稍后重试: " + ex.getMessage());
            }
            mallIdempotencyService.cacheOrderNo(userId, idempotencyKey, orderNo);
            return new OrderResultDto(orderNo, OrderEntity.STATUS_PENDING);
        } finally {
            mallIdempotencyService.end(userId, idempotencyKey);
        }
    }

    private void assertInSeckillWindow(GoodsEntity g) {
        LocalDateTime now = LocalDateTime.now();
        if (g.getBeginTime() != null && now.isBefore(g.getBeginTime())) {
            throw new BizException("秒杀尚未开始");
        }
        if (g.getEndTime() != null && now.isAfter(g.getEndTime())) {
            throw new BizException("秒杀已结束");
        }
    }
}
