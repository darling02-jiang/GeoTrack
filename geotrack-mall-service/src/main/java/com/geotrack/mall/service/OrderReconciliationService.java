package com.geotrack.mall.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.geotrack.common.mq.RocketMqTopics;
import com.geotrack.mall.entity.GoodsEntity;
import com.geotrack.mall.entity.MqConsumeLogEntity;
import com.geotrack.mall.entity.OrderEntity;
import com.geotrack.mall.entity.PointFlowEntity;
import com.geotrack.mall.mapper.GoodsMapper;
import com.geotrack.mall.mapper.OrderMapper;
import com.geotrack.mall.mapper.PointFlowMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderReconciliationService {

    private static final Logger log = LoggerFactory.getLogger(OrderReconciliationService.class);
    private static final String BIZ_TYPE_MALL_ORDER = "MALL_ORDER";

    private final OrderMapper orderMapper;
    private final GoodsMapper goodsMapper;
    private final PointFlowMapper pointFlowMapper;
    private final MallOrderTxnService mallOrderTxnService;
    private final MqConsumeLogService mqConsumeLogService;

    public OrderReconciliationService(
            OrderMapper orderMapper,
            GoodsMapper goodsMapper,
            PointFlowMapper pointFlowMapper,
            MallOrderTxnService mallOrderTxnService,
            MqConsumeLogService mqConsumeLogService
    ) {
        this.orderMapper = orderMapper;
        this.goodsMapper = goodsMapper;
        this.pointFlowMapper = pointFlowMapper;
        this.mallOrderTxnService = mallOrderTxnService;
        this.mqConsumeLogService = mqConsumeLogService;
    }

    public int reconcilePaidPendingOrders(int minPendingSeconds, int batchSize) {
        LocalDateTime deadline = LocalDateTime.now().minusSeconds(Math.max(minPendingSeconds, 1));
        List<OrderEntity> rows = orderMapper.selectList(new LambdaQueryWrapper<OrderEntity>()
                .eq(OrderEntity::getStatus, OrderEntity.STATUS_PENDING)
                .le(OrderEntity::getCreatedAt, deadline)
                .orderByAsc(OrderEntity::getId)
                .last("LIMIT " + Math.min(Math.max(batchSize, 1), 200)));
        int fixed = 0;
        for (OrderEntity order : rows) {
            if (hasDeductFlow(order)) {
                if (settlePaid(order)) {
                    fixed++;
                }
            }
        }
        return fixed;
    }

    public int retryFailedPointResultLogs(int limit) {
        List<MqConsumeLogEntity> failed = mqConsumeLogService.listFailed(
                RocketMqTopics.MALL_POINTS_RESULT_CONSUMER_GROUP,
                limit
        );
        int fixed = 0;
        for (MqConsumeLogEntity row : failed) {
            OrderEntity order = mallOrderTxnService.findByOrderNo(row.getMessageKey());
            if (order == null || !OrderEntity.STATUS_PENDING.equals(order.getStatus()) || !hasDeductFlow(order)) {
                continue;
            }
            if (settlePaid(order)) {
                mqConsumeLogService.markSuccess(row.getConsumerGroup(), row.getMessageKey());
                fixed++;
            }
        }
        return fixed;
    }

    private boolean settlePaid(OrderEntity order) {
        GoodsEntity goods = goodsMapper.selectById(order.getGoodsId());
        if (goods == null) {
            log.warn("ORDER_RECONCILE_SKIP_NO_GOODS orderNo={}, goodsId={}", order.getOrderNo(), order.getGoodsId());
            return false;
        }
        boolean seckill = goods.getIsSeckill() != null && goods.getIsSeckill() != 0;
        if (seckill) {
            mallOrderTxnService.settleSeckillOrder(order.getOrderNo(), true);
        } else {
            mallOrderTxnService.markOrderPaid(order.getId());
        }
        log.info("ORDER_RECONCILED_PAID orderNo={}, seckill={}", order.getOrderNo(), seckill);
        return true;
    }

    private boolean hasDeductFlow(OrderEntity order) {
        Long count = pointFlowMapper.selectCount(new LambdaQueryWrapper<PointFlowEntity>()
                .eq(PointFlowEntity::getUserId, order.getUserId())
                .eq(PointFlowEntity::getBizType, BIZ_TYPE_MALL_ORDER)
                .eq(PointFlowEntity::getBizNo, order.getOrderNo()));
        return count != null && count > 0;
    }
}
