package com.geotrack.mall.task;

import com.geotrack.mall.service.OrderReconciliationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OrderReconciliationTask {

    private static final Logger log = LoggerFactory.getLogger(OrderReconciliationTask.class);

    private final OrderReconciliationService orderReconciliationService;

    @Value("${geotrack.order.reconcile-min-pending-seconds:60}")
    private int minPendingSeconds;

    @Value("${geotrack.order.reconcile-batch-size:100}")
    private int batchSize;

    public OrderReconciliationTask(OrderReconciliationService orderReconciliationService) {
        this.orderReconciliationService = orderReconciliationService;
    }

    @Scheduled(fixedDelayString = "${geotrack.order.reconcile-fixed-delay-ms:60000}")
    public void reconcileOrders() {
        int fixed = orderReconciliationService.reconcilePaidPendingOrders(minPendingSeconds, batchSize);
        if (fixed > 0) {
            log.info("ORDER_RECONCILE_TASK fixed={}", fixed);
        }
    }
}
