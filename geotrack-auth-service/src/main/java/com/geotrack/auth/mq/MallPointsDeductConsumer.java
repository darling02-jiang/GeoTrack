package com.geotrack.auth.mq;

import com.geotrack.common.exception.BizException;
import com.geotrack.common.mq.MallPointsDeductMessage;
import com.geotrack.common.mq.MallPointsDeductResultMessage;
import com.geotrack.common.mq.RocketMqTopics;
import com.geotrack.auth.service.MqConsumeLogService;
import com.geotrack.auth.service.PointsService;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@RocketMQMessageListener(
        topic = RocketMqTopics.MALL_POINTS_DEDUCT_TOPIC,
        consumerGroup = RocketMqTopics.AUTH_MALL_POINTS_CONSUMER_GROUP,
        nameServer = "${rocketmq.name-server:127.0.0.1:9876}"
)
public class MallPointsDeductConsumer implements RocketMQListener<MallPointsDeductMessage> {

    private static final Logger log = LoggerFactory.getLogger(MallPointsDeductConsumer.class);

    private final MqConsumeLogService mqConsumeLogService;
    private final PointsService pointsService;
    private final RocketMQTemplate rocketMQTemplate;

    public MallPointsDeductConsumer(
            MqConsumeLogService mqConsumeLogService,
            PointsService pointsService,
            RocketMQTemplate rocketMQTemplate
    ) {
        this.mqConsumeLogService = mqConsumeLogService;
        this.pointsService = pointsService;
        this.rocketMQTemplate = rocketMQTemplate;
    }

    @Override
    public void onMessage(MallPointsDeductMessage message) {
        if (message == null) {
            return;
        }
        String key = message.getMessageKey();
        if (!mqConsumeLogService.begin(RocketMqTopics.AUTH_MALL_POINTS_CONSUMER_GROUP, key)) {
            return;
        }
        try {
            int points = message.getPoints() == null ? 0 : message.getPoints();
            try {
                pointsService.deductMallOrderPoints(message.getUserId(), message.getOrderNo(), points);
                publishResult(message, true, "积分扣减成功");
            } catch (BizException ex) {
                publishResult(message, false, ex.getMessage());
            }
            mqConsumeLogService.success(RocketMqTopics.AUTH_MALL_POINTS_CONSUMER_GROUP, key);
            log.info("MALL_POINTS_DEDUCT_CONSUMED orderNo={}, userId={}, points={}",
                    message.getOrderNo(), message.getUserId(), points);
        } catch (Exception ex) {
            mqConsumeLogService.failed(RocketMqTopics.AUTH_MALL_POINTS_CONSUMER_GROUP, key, ex.getMessage());
            throw ex;
        }
    }

    private void publishResult(MallPointsDeductMessage message, boolean success, String reason) {
        MallPointsDeductResultMessage result = new MallPointsDeductResultMessage(
                message.getOrderNo(),
                message.getOrderNo(),
                message.getUserId(),
                message.getGoodsId(),
                success,
                reason
        );
        rocketMQTemplate.convertAndSend(RocketMqTopics.MALL_POINTS_DEDUCT_RESULT_TOPIC, result);
    }
}
