package com.geotrack.mall.task;

import com.geotrack.mall.service.MqConsumeLogService;
import com.geotrack.mall.service.OrderReconciliationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class MqFailureRetryTask {

    private static final Logger log = LoggerFactory.getLogger(MqFailureRetryTask.class);

    private final MqConsumeLogService mqConsumeLogService;
    private final OrderReconciliationService orderReconciliationService;

    @Value("${geotrack.mq.processing-timeout-minutes:10}")
    private long processingTimeoutMinutes;

    @Value("${geotrack.mq.failed-retry-batch-size:100}")
    private int failedRetryBatchSize;

    public MqFailureRetryTask(MqConsumeLogService mqConsumeLogService, OrderReconciliationService orderReconciliationService) {
        this.mqConsumeLogService = mqConsumeLogService;
        this.orderReconciliationService = orderReconciliationService;
    }

    @Scheduled(fixedDelayString = "${geotrack.mq.failed-retry-fixed-delay-ms:120000}")
    public void retryFailedMqEffects() {
        int stale = mqConsumeLogService.failStaleProcessing(Duration.ofMinutes(processingTimeoutMinutes));
        int fixed = orderReconciliationService.retryFailedPointResultLogs(failedRetryBatchSize);
        if (stale > 0 || fixed > 0) {
            log.info("MQ_FAILURE_RETRY_TASK staleProcessing={}, fixedPointResult={}", stale, fixed);
        }
    }
}
