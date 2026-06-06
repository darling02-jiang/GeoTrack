package com.geotrack.poi.service;

import com.geotrack.common.mq.CheckInFeedCreateMessage;
import com.geotrack.common.mq.CheckInPointsGrantMessage;
import com.geotrack.common.mq.RocketMqTopics;
import com.geotrack.poi.entity.CheckInRecordEntity;
import com.geotrack.poi.entity.PoiEntity;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * 打卡成功后的下游动作编排：事务提交后投递 RocketMQ 消息，由认证服务发积分、内容服务生成动态。
 */
@Component
public class CheckInEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(CheckInEventPublisher.class);
    private final RocketMQTemplate rocketMQTemplate;

    public CheckInEventPublisher(RocketMQTemplate rocketMQTemplate) {
        this.rocketMQTemplate = rocketMQTemplate;
    }

    public void publishPointsGrantEvent(CheckInRecordEntity record, PoiEntity poi) {
        int reward = poi.getRewardPoints() == null ? 0 : poi.getRewardPoints();
        if (reward <= 0) {
            return;
        }
        CheckInPointsGrantMessage message = new CheckInPointsGrantMessage(record.getUserId(), record.getId(), reward);
        sendAfterCommit(RocketMqTopics.CHECKIN_POINTS_TOPIC, message, "POINTS_GRANT_EVENT");
    }

    public void publishFeedCreateEvent(CheckInRecordEntity record, String content) {
        CheckInFeedCreateMessage message = new CheckInFeedCreateMessage(
                record.getId(),
                record.getUserId(),
                record.getPoiId(),
                content,
                record.getImageUrl()
        );
        sendAfterCommit(RocketMqTopics.CHECKIN_FEED_TOPIC, message, "FEED_CREATE_EVENT");
    }

    private void sendAfterCommit(String topic, Object message, String eventName) {
        Runnable sendTask = () -> {
            rocketMQTemplate.convertAndSend(topic, message);
            log.info("{}_SENT topic={}, payload={}", eventName, topic, message.getClass().getSimpleName());
        };
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    sendTask.run();
                }
            });
        } else {
            sendTask.run();
        }
    }
}
