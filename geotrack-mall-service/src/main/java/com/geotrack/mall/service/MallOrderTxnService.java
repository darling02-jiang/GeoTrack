package com.geotrack.mall.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.geotrack.common.exception.BizException;
import com.geotrack.mall.entity.GoodsEntity;
import com.geotrack.mall.entity.OrderEntity;
import com.geotrack.mall.entity.OrderStatusLogEntity;
import com.geotrack.mall.mapper.GoodsMapper;
import com.geotrack.mall.mapper.OrderMapper;
import com.geotrack.mall.mapper.OrderStatusLogMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单与库存事务边界：普通兑换在同事务内 {@code SELECT ... FOR UPDATE} 扣 {@code gt_goods.stock}；
 * 秒杀仅在支付成功后 {@link #markSeckillPaidAndDecDbStock} 同步扣 DB，与 Redis 预减配合。
 */
@Service
public class MallOrderTxnService {

    private final GoodsMapper goodsMapper;
    private final OrderMapper orderMapper;
    private final OrderStatusLogMapper orderStatusLogMapper;

    public MallOrderTxnService(GoodsMapper goodsMapper, OrderMapper orderMapper, OrderStatusLogMapper orderStatusLogMapper) {
        this.goodsMapper = goodsMapper;
        this.orderMapper = orderMapper;
        this.orderStatusLogMapper = orderStatusLogMapper;
    }

    @Transactional(rollbackFor = Exception.class)
    public void createExchangeOrder(Long userId, Long goodsId, String orderNo) {
        GoodsEntity g = lockGoods(goodsId);
        if (g.getStatus() == null || g.getStatus() != 1) {
            throw new BizException("商品已下架");
        }
        if (g.getIsSeckill() != null && g.getIsSeckill() != 0) {
            throw new BizException("秒杀商品请走秒杀通道");
        }
        int stock = g.getStock() == null ? 0 : g.getStock();
        if (stock < 1) {
            throw new BizException("库存不足");
        }
        int price = g.getPointsPrice() == null ? 0 : g.getPointsPrice();
        if (price <= 0) {
            throw new BizException("商品价格异常");
        }
        g.setStock(stock - 1);
        goodsMapper.updateById(g);

        OrderEntity o = new OrderEntity();
        o.setOrderNo(orderNo);
        o.setUserId(userId);
        o.setGoodsId(goodsId);
        o.setPointsCost(price);
        o.setStatus(OrderEntity.STATUS_PENDING);
        orderMapper.insert(o);
        appendStatusLog(orderNo, null, OrderEntity.STATUS_PENDING, "普通兑换订单创建");
    }

    @Transactional(rollbackFor = Exception.class)
    public void createSeckillPendingOrder(Long userId, Long goodsId, String orderNo) {
        createSeckillPendingOrderIfAbsent(userId, goodsId, orderNo);
    }

    @Transactional(rollbackFor = Exception.class)
    public OrderEntity createSeckillPendingOrderIfAbsent(Long userId, Long goodsId, String orderNo) {
        OrderEntity existed = findByOrderNo(orderNo);
        if (existed != null) {
            return existed;
        }
        GoodsEntity g = goodsMapper.selectById(goodsId);
        if (g == null) {
            throw new BizException("商品不存在");
        }
        if (g.getStatus() == null || g.getStatus() != 1) {
            throw new BizException("商品已下架");
        }
        if (g.getIsSeckill() == null || g.getIsSeckill() == 0) {
            throw new BizException("非秒杀商品");
        }
        int price = g.getPointsPrice() == null ? 0 : g.getPointsPrice();
        if (price <= 0) {
            throw new BizException("商品价格异常");
        }
        OrderEntity o = new OrderEntity();
        o.setOrderNo(orderNo);
        o.setUserId(userId);
        o.setGoodsId(goodsId);
        o.setPointsCost(price);
        o.setStatus(OrderEntity.STATUS_PENDING);
        orderMapper.insert(o);
        appendStatusLog(orderNo, null, OrderEntity.STATUS_PENDING, "秒杀订单异步创建");
        return o;
    }

    @Transactional(rollbackFor = Exception.class)
    public void markOrderPaid(Long orderId) {
        changeStatus(orderId, OrderEntity.STATUS_PAID, "积分扣减成功");
    }

    @Transactional(rollbackFor = Exception.class)
    public void markSeckillPaidAndDecDbStock(Long orderId, Long goodsId) {
        GoodsEntity g = lockGoods(goodsId);
        int stock = g.getStock() == null ? 0 : g.getStock();
        if (stock < 1) {
            throw new BizException("数据库库存不足，请稍后重试或联系客服");
        }
        g.setStock(stock - 1);
        goodsMapper.updateById(g);
        changeStatus(orderId, OrderEntity.STATUS_PAID, "秒杀订单结算成功");
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean settleSeckillOrder(String orderNo, boolean success) {
        OrderEntity order = orderMapper.selectOne(new LambdaQueryWrapper<OrderEntity>()
                .eq(OrderEntity::getOrderNo, orderNo)
                .last("FOR UPDATE"));
        if (order == null) {
            throw new BizException("订单不存在");
        }
        if (OrderEntity.STATUS_PAID.equals(order.getStatus()) || OrderEntity.STATUS_CANCELLED.equals(order.getStatus())) {
            return false;
        }
        if (!OrderEntity.STATUS_PENDING.equals(order.getStatus())) {
            throw new BizException("订单状态异常: " + order.getStatus());
        }
        if (!success) {
            changeStatus(order.getId(), OrderEntity.STATUS_CANCELLED, "积分扣减失败");
            return true;
        }

        GoodsEntity g = lockGoods(order.getGoodsId());
        int stock = g.getStock() == null ? 0 : g.getStock();
        if (stock < 1) {
            throw new BizException("数据库库存不足，请稍后重试或联系客服");
        }
        g.setStock(stock - 1);
        goodsMapper.updateById(g);

        changeStatus(order.getId(), OrderEntity.STATUS_PAID, "秒杀订单结算成功");
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    public void cancelPendingOrder(String orderNo, boolean restoreExchangeStock) {
        OrderEntity o = orderMapper.selectOne(new LambdaQueryWrapper<OrderEntity>()
                .eq(OrderEntity::getOrderNo, orderNo)
                .eq(OrderEntity::getStatus, OrderEntity.STATUS_PENDING));
        if (o == null) {
            return;
        }
        if (restoreExchangeStock) {
            GoodsEntity g = lockGoods(o.getGoodsId());
            int stock = g.getStock() == null ? 0 : g.getStock();
            g.setStock(stock + 1);
            goodsMapper.updateById(g);
        }
        changeStatus(o.getId(), OrderEntity.STATUS_CANCELLED, "订单取消");
    }

    @Transactional(rollbackFor = Exception.class)
    public int cancelExpiredPendingOrders(int timeoutMinutes, int batchSize, SeckillRedisStockService seckillRedisStockService) {
        LocalDateTime deadline = LocalDateTime.now().minusMinutes(Math.max(timeoutMinutes, 1));
        int size = Math.min(Math.max(batchSize, 1), 200);
        List<OrderEntity> rows = orderMapper.selectList(new LambdaQueryWrapper<OrderEntity>()
                .eq(OrderEntity::getStatus, OrderEntity.STATUS_PENDING)
                .le(OrderEntity::getCreatedAt, deadline)
                .orderByAsc(OrderEntity::getId)
                .last("LIMIT " + size));
        int changed = 0;
        for (OrderEntity order : rows) {
            OrderEntity locked = orderMapper.selectOne(new LambdaQueryWrapper<OrderEntity>()
                    .eq(OrderEntity::getId, order.getId())
                    .last("FOR UPDATE"));
            if (locked == null || !OrderEntity.STATUS_PENDING.equals(locked.getStatus())) {
                continue;
            }
            GoodsEntity goods = lockGoods(locked.getGoodsId());
            boolean seckill = goods.getIsSeckill() != null && goods.getIsSeckill() != 0;
            if (seckill) {
                seckillRedisStockService.compensate(locked.getGoodsId(), locked.getUserId());
            } else {
                int stock = goods.getStock() == null ? 0 : goods.getStock();
                goods.setStock(stock + 1);
                goodsMapper.updateById(goods);
            }
            changeStatus(locked.getId(), OrderEntity.STATUS_CANCELLED, "订单超时自动取消");
            changed++;
        }
        return changed;
    }

    /** 行锁防并发超卖（普通兑换路径）；与 MySQL InnoDB 事务同行 */
    private GoodsEntity lockGoods(Long goodsId) {
        GoodsEntity g = goodsMapper.selectOne(new LambdaQueryWrapper<GoodsEntity>()
                .eq(GoodsEntity::getId, goodsId)
                .last("FOR UPDATE"));
        if (g == null) {
            throw new BizException("商品不存在");
        }
        return g;
    }

    public OrderEntity findByOrderNo(String orderNo) {
        return orderMapper.selectOne(new LambdaQueryWrapper<OrderEntity>()
                .eq(OrderEntity::getOrderNo, orderNo));
    }

    private void changeStatus(Long orderId, String toStatus, String reason) {
        OrderEntity current = orderMapper.selectById(orderId);
        if (current == null) {
            throw new BizException("订单不存在");
        }
        String from = current.getStatus();
        if (toStatus.equals(from)) {
            return;
        }
        OrderEntity u = new OrderEntity();
        u.setId(orderId);
        u.setStatus(toStatus);
        orderMapper.updateById(u);
        appendStatusLog(current.getOrderNo(), from, toStatus, reason);
    }

    private void appendStatusLog(String orderNo, String fromStatus, String toStatus, String reason) {
        OrderStatusLogEntity log = new OrderStatusLogEntity();
        log.setOrderNo(orderNo);
        log.setFromStatus(fromStatus);
        log.setToStatus(toStatus);
        log.setReason(reason);
        orderStatusLogMapper.insert(log);
    }
}
