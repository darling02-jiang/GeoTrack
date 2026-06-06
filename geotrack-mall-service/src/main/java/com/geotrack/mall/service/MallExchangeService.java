package com.geotrack.mall.service;

import com.geotrack.common.exception.BizException;
import com.geotrack.mall.dto.OrderResultDto;
import com.geotrack.mall.entity.OrderEntity;
import com.geotrack.mall.util.OrderNoGenerator;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 普通兑换：DB 行锁商品行扣库存 + PENDING 订单 → 调认证服务扣积分 → PAID；扣积分失败则回滚库存（{@code cancelPendingOrder(..., true)}）。
 * 与秒杀分流，禁止秒杀商品走本通道。
 */
@Service
public class MallExchangeService {

    private final MallOrderTxnService mallOrderTxnService;
    private final AuthPointsDeductService authPointsDeductService;
    private final MallIdempotencyService mallIdempotencyService;

    public MallExchangeService(
            MallOrderTxnService mallOrderTxnService,
            AuthPointsDeductService authPointsDeductService,
            MallIdempotencyService mallIdempotencyService
    ) {
        this.mallOrderTxnService = mallOrderTxnService;
        this.authPointsDeductService = authPointsDeductService;
        this.mallIdempotencyService = mallIdempotencyService;
    }

    public OrderResultDto exchange(Long userId, Long goodsId, String idempotencyKey) {
        if (userId == null || goodsId == null) {
            throw new BizException("参数不完整");
        }
        if (!StringUtils.hasText(idempotencyKey)) {
            throw new BizException("请携带幂等键 X-Idempotency-Key");
        }
        String cached = mallIdempotencyService.getCachedOrderNo(userId, idempotencyKey);
        if (cached != null) {
            OrderEntity existed = mallOrderTxnService.findByOrderNo(cached);
            if (existed != null && OrderEntity.STATUS_PAID.equals(existed.getStatus())) {
                return new OrderResultDto(cached, OrderEntity.STATUS_PAID);
            }
        }
        if (!mallIdempotencyService.tryBegin(userId, idempotencyKey)) {
            throw new BizException("请求处理中，请稍后再试");
        }
        try {
            String orderNo = OrderNoGenerator.next();
            // 先占库存再扣积分：积分服务幂等按 orderNo，失败可安全回滚库存
            mallOrderTxnService.createExchangeOrder(userId, goodsId, orderNo);
            OrderEntity order = mallOrderTxnService.findByOrderNo(orderNo);
            if (order == null) {
                throw new BizException("订单创建失败");
            }
            try {
                authPointsDeductService.deductMallOrderPoints(userId, orderNo, order.getPointsCost());
            } catch (Exception ex) {
                mallOrderTxnService.cancelPendingOrder(orderNo, true);
                throw ex;
            }
            mallOrderTxnService.markOrderPaid(order.getId());
            mallIdempotencyService.cacheOrderNo(userId, idempotencyKey, orderNo);
            return new OrderResultDto(orderNo, OrderEntity.STATUS_PAID);
        } finally {
            mallIdempotencyService.end(userId, idempotencyKey);
        }
    }
}
