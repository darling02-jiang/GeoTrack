package com.geotrack.content.mq;

import com.geotrack.common.mq.CheckInFeedCreateMessage;
import com.geotrack.common.mq.RocketMqTopics;
import com.geotrack.content.dto.FeedCreateRequest;
import com.geotrack.content.service.FeedService;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 消费打卡动态消息；FeedService 使用 checkInRecordId 做动态生成幂等。
 */
@Component
@RocketMQMessageListener(
        topic = RocketMqTopics.CHECKIN_FEED_TOPIC,
        consumerGroup = RocketMqTopics.CONTENT_FEED_CONSUMER_GROUP
)
public class CheckInFeedCreateConsumer implements RocketMQListener<CheckInFeedCreateMessage> {

    private static final Logger log = LoggerFactory.getLogger(CheckInFeedCreateConsumer.class);

    private final FeedService feedService;

    public CheckInFeedCreateConsumer(FeedService feedService) {
        this.feedService = feedService;
    }

    @Override
    public void onMessage(CheckInFeedCreateMessage message) {
        if (message == null) {
            return;
        }
        Long feedId = feedService.createFromCheckIn(new FeedCreateRequest(
                message.getCheckInRecordId(),
                message.getUserId(),
                message.getPoiId(),
                message.getContent(),
                message.getImageUrl()
        ));
        log.info("CHECKIN_FEED_CONSUMED checkInRecordId={}, feedId={}", message.getCheckInRecordId(), feedId);
    }
}
