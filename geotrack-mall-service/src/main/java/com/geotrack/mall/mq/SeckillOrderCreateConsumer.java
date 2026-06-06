package com.geotrack.mall.mq;

import com.geotrack.common.mq.MallPointsDeductMessage;
import com.geotrack.common.mq.RocketMqTopics;
import com.geotrack.common.mq.SeckillOrderCreateMessage;
import com.geotrack.common.exception.BizException;
import com.geotrack.mall.entity.OrderEntity;
import com.geotrack.mall.service.MallOrderTxnService;
import com.geotrack.mall.service.MqConsumeLogService;
import com.geotrack.mall.service.SeckillRedisStockService;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@RocketMQMessageListener(
        topic = RocketMqTopics.SECKILL_ORDER_CREATE_TOPIC,
        consumerGroup = RocketMqTopics.MALL_SECKILL_ORDER_CONSUMER_GROUP
)
public class SeckillOrderCreateConsumer implements RocketMQListener<SeckillOrderCreateMessage> {

    private static final Logger log = LoggerFactory.getLogger(SeckillOrderCreateConsumer.class);

    private final MqConsumeLogService mqConsumeLogService;
    private final MallOrderTxnService mallOrderTxnService;
    private final SeckillRedisStockService seckillRedisStockService;
    private final RocketMQTemplate rocketMQTemplate;

    public SeckillOrderCreateConsumer(
            MqConsumeLogService mqConsumeLogService,
            MallOrderTxnService mallOrderTxnService,
            SeckillRedisStockService seckillRedisStockService,
            RocketMQTemplate rocketMQTemplate
    ) {
        this.mqConsumeLogService = mqConsumeLogService;
        this.mallOrderTxnService = mallOrderTxnService;
        this.seckillRedisStockService = seckillRedisStockService;
        this.rocketMQTemplate = rocketMQTemplate;
    }

    @Override
    public void onMessage(SeckillOrderCreateMessage message) {
        if (message == null) {
            return;
        }
        String key = message.getMessageKey();
        if (!mqConsumeLogService.begin(RocketMqTopics.MALL_SECKILL_ORDER_CONSUMER_GROUP, key)) {
            return;
        }
        try {
            OrderEntity order = mallOrderTxnService.createSeckillPendingOrderIfAbsent(
                    message.getUserId(),
                    message.getGoodsId(),
                    message.getOrderNo()
            );
            MallPointsDeductMessage deductMessage = new MallPointsDeductMessage(
                    order.getOrderNo(),
                    order.getOrderNo(),
                    order.getUserId(),
                    order.getGoodsId(),
                    order.getPointsCost()
            );
            rocketMQTemplate.convertAndSend(RocketMqTopics.MALL_POINTS_DEDUCT_TOPIC, deductMessage);
            mqConsumeLogService.success(RocketMqTopics.MALL_SECKILL_ORDER_CONSUMER_GROUP, key);
            log.info("SECKILL_ORDER_CREATED_ASYNC orderNo={}, userId={}, goodsId={}, points={}",
                    order.getOrderNo(), order.getUserId(), order.getGoodsId(), order.getPointsCost());
        } catch (BizException ex) {
            seckillRedisStockService.compensate(message.getGoodsId(), message.getUserId());
            mqConsumeLogService.success(RocketMqTopics.MALL_SECKILL_ORDER_CONSUMER_GROUP, key);
            log.warn("SECKILL_ORDER_CREATE_REJECTED orderNo={}, reason={}", message.getOrderNo(), ex.getMessage());
        } catch (Exception ex) {
            mqConsumeLogService.failed(RocketMqTopics.MALL_SECKILL_ORDER_CONSUMER_GROUP, key, ex.getMessage());
            throw ex;
        }
    }
}
