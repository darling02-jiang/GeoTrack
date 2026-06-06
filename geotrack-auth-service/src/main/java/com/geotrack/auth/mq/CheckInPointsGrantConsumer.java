package com.geotrack.auth.mq;

import com.geotrack.auth.service.PointsService;
import com.geotrack.common.mq.CheckInPointsGrantMessage;
import com.geotrack.common.mq.RocketMqTopics;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 消费打卡积分消息；PointsService 使用 userId + CHECKIN + checkInRecordId 做流水幂等。
 */
@Component
@RocketMQMessageListener(
        topic = RocketMqTopics.CHECKIN_POINTS_TOPIC,
        consumerGroup = RocketMqTopics.AUTH_POINTS_CONSUMER_GROUP,
        nameServer = "${rocketmq.name-server:127.0.0.1:9876}"
)
public class CheckInPointsGrantConsumer implements RocketMQListener<CheckInPointsGrantMessage> {

    private static final Logger log = LoggerFactory.getLogger(CheckInPointsGrantConsumer.class);

    private final PointsService pointsService;

    public CheckInPointsGrantConsumer(PointsService pointsService) {
        this.pointsService = pointsService;
    }

    @Override
    public void onMessage(CheckInPointsGrantMessage message) {
        if (message == null) {
            return;
        }
        int points = message.getPoints() == null ? 0 : message.getPoints();
        pointsService.grantCheckInPoints(message.getUserId(), message.getCheckInRecordId(), points);
        log.info("CHECKIN_POINTS_CONSUMED userId={}, checkInRecordId={}, points={}",
                message.getUserId(), message.getCheckInRecordId(), points);
    }
}
