package com.geotrack.mall.task;

import com.geotrack.mall.service.MallOrderTxnService;
import com.geotrack.mall.service.SeckillRedisStockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OrderTimeoutCancelTask {

    private static final Logger log = LoggerFactory.getLogger(OrderTimeoutCancelTask.class);

    private final MallOrderTxnService mallOrderTxnService;
    private final SeckillRedisStockService seckillRedisStockService;

    @Value("${geotrack.order.pending-timeout-minutes:15}")
    private int pendingTimeoutMinutes;

    @Value("${geotrack.order.timeout-cancel-batch-size:100}")
    private int batchSize;

    public OrderTimeoutCancelTask(MallOrderTxnService mallOrderTxnService, SeckillRedisStockService seckillRedisStockService) {
        this.mallOrderTxnService = mallOrderTxnService;
        this.seckillRedisStockService = seckillRedisStockService;
    }

    @Scheduled(fixedDelayString = "${geotrack.order.timeout-cancel-fixed-delay-ms:60000}")
    public void cancelExpiredPendingOrders() {
        int count = mallOrderTxnService.cancelExpiredPendingOrders(
                pendingTimeoutMinutes,
                batchSize,
                seckillRedisStockService
        );
        if (count > 0) {
            log.info("ORDER_TIMEOUT_CANCELLED count={}", count);
        }
    }
}
