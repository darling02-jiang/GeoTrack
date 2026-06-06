package com.geotrack.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.geotrack.auth.entity.MqConsumeLogEntity;
import com.geotrack.auth.mapper.MqConsumeLogMapper;
import com.geotrack.common.exception.BizException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;

@Service
public class MqConsumeLogService {

    private static final String LOCK_PREFIX = "mq:consume:lock:";

    private final MqConsumeLogMapper mqConsumeLogMapper;
    private final StringRedisTemplate redisTemplate;

    public MqConsumeLogService(MqConsumeLogMapper mqConsumeLogMapper, StringRedisTemplate redisTemplate) {
        this.mqConsumeLogMapper = mqConsumeLogMapper;
        this.redisTemplate = redisTemplate;
    }

    public boolean begin(String consumerGroup, String messageKey) {
        if (!StringUtils.hasText(consumerGroup) || !StringUtils.hasText(messageKey)) {
            throw new BizException("MQ 消费幂等键不完整");
        }
        MqConsumeLogEntity existed = find(consumerGroup, messageKey);
        if (existed != null && MqConsumeLogEntity.STATUS_SUCCESS.equals(existed.getStatus())) {
            return false;
        }
        Boolean locked = redisTemplate.opsForValue()
                .setIfAbsent(lockKey(consumerGroup, messageKey), "1", Duration.ofMinutes(5));
        if (!Boolean.TRUE.equals(locked)) {
            throw new BizException("消息正在消费中，等待重试");
        }
        if (existed == null) {
            MqConsumeLogEntity row = new MqConsumeLogEntity();
            row.setConsumerGroup(consumerGroup);
            row.setMessageKey(messageKey);
            row.setStatus(MqConsumeLogEntity.STATUS_PROCESSING);
            try {
                mqConsumeLogMapper.insert(row);
            } catch (DuplicateKeyException ignored) {
                // 并发插入时以唯一索引为最终防线。
            }
        } else {
            updateStatus(consumerGroup, messageKey, MqConsumeLogEntity.STATUS_PROCESSING, null);
        }
        return true;
    }

    public void success(String consumerGroup, String messageKey) {
        updateStatus(consumerGroup, messageKey, MqConsumeLogEntity.STATUS_SUCCESS, null);
        redisTemplate.delete(lockKey(consumerGroup, messageKey));
    }

    public void failed(String consumerGroup, String messageKey, String errorMessage) {
        updateStatus(consumerGroup, messageKey, MqConsumeLogEntity.STATUS_FAILED, trim(errorMessage));
        redisTemplate.delete(lockKey(consumerGroup, messageKey));
    }

    private MqConsumeLogEntity find(String consumerGroup, String messageKey) {
        return mqConsumeLogMapper.selectOne(new LambdaQueryWrapper<MqConsumeLogEntity>()
                .eq(MqConsumeLogEntity::getConsumerGroup, consumerGroup)
                .eq(MqConsumeLogEntity::getMessageKey, messageKey)
                .last("limit 1"));
    }

    private void updateStatus(String consumerGroup, String messageKey, String status, String errorMessage) {
        mqConsumeLogMapper.update(null, new LambdaUpdateWrapper<MqConsumeLogEntity>()
                .eq(MqConsumeLogEntity::getConsumerGroup, consumerGroup)
                .eq(MqConsumeLogEntity::getMessageKey, messageKey)
                .set(MqConsumeLogEntity::getStatus, status)
                .set(MqConsumeLogEntity::getErrorMessage, errorMessage));
    }

    private String lockKey(String consumerGroup, String messageKey) {
        return LOCK_PREFIX + consumerGroup + ":" + messageKey;
    }

    private String trim(String message) {
        if (message == null) {
            return null;
        }
        return message.length() <= 500 ? message : message.substring(0, 500);
    }
}
