package com.geotrack.mall.mq;

import com.geotrack.common.mq.MallPointsDeductResultMessage;
import com.geotrack.common.mq.RocketMqTopics;
import com.geotrack.mall.entity.OrderEntity;
import com.geotrack.mall.service.MallOrderTxnService;
import com.geotrack.mall.service.MqConsumeLogService;
import com.geotrack.mall.service.SeckillRedisStockService;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@RocketMQMessageListener(
        topic = RocketMqTopics.MALL_POINTS_DEDUCT_RESULT_TOPIC,
        consumerGroup = RocketMqTopics.MALL_POINTS_RESULT_CONSUMER_GROUP
)
public class MallPointsDeductResultConsumer implements RocketMQListener<MallPointsDeductResultMessage> {

    private static final Logger log = LoggerFactory.getLogger(MallPointsDeductResultConsumer.class);

    private final MqConsumeLogService mqConsumeLogService;
    private final MallOrderTxnService mallOrderTxnService;
    private final SeckillRedisStockService seckillRedisStockService;

    public MallPointsDeductResultConsumer(
            MqConsumeLogService mqConsumeLogService,
            MallOrderTxnService mallOrderTxnService,
            SeckillRedisStockService seckillRedisStockService
    ) {
        this.mqConsumeLogService = mqConsumeLogService;
        this.mallOrderTxnService = mallOrderTxnService;
        this.seckillRedisStockService = seckillRedisStockService;
    }

    @Override
    public void onMessage(MallPointsDeductResultMessage message) {
        if (message == null) {
            return;
        }
        String key = message.getMessageKey();
        if (!mqConsumeLogService.begin(RocketMqTopics.MALL_POINTS_RESULT_CONSUMER_GROUP, key)) {
            return;
        }
        try {
            boolean success = Boolean.TRUE.equals(message.getSuccess());
            boolean changed = mallOrderTxnService.settleSeckillOrder(message.getOrderNo(), success);
            if (changed && !success) {
                seckillRedisStockService.compensate(message.getGoodsId(), message.getUserId());
            }
            mqConsumeLogService.success(RocketMqTopics.MALL_POINTS_RESULT_CONSUMER_GROUP, key);
            log.info("SECKILL_ORDER_SETTLED orderNo={}, success={}, changed={}, reason={}",
                    message.getOrderNo(), success, changed, message.getReason());
        } catch (Exception ex) {
            mqConsumeLogService.failed(RocketMqTopics.MALL_POINTS_RESULT_CONSUMER_GROUP, key, ex.getMessage());
            throw ex;
        }
    }
}
