package com.geotrack.order.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.geotrack.common.exception.BizException;
import com.geotrack.order.dto.OrderDetailDto;
import com.geotrack.order.dto.OrderListItemDto;
import com.geotrack.order.dto.OrderStatusLogDto;
import com.geotrack.order.entity.OrderEntity;
import com.geotrack.order.entity.OrderStatusLogEntity;
import com.geotrack.order.mapper.OrderMapper;
import com.geotrack.order.mapper.OrderStatusLogMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderQueryService {

    private final OrderMapper orderMapper;
    private final OrderStatusLogMapper orderStatusLogMapper;

    public OrderQueryService(OrderMapper orderMapper, OrderStatusLogMapper orderStatusLogMapper) {
        this.orderMapper = orderMapper;
        this.orderStatusLogMapper = orderStatusLogMapper;
    }

    public List<OrderListItemDto> listMyOrders(Long userId, int limit) {
        int size = Math.min(Math.max(limit, 1), 200);
        List<OrderEntity> list = orderMapper.selectList(new LambdaQueryWrapper<OrderEntity>()
                .eq(OrderEntity::getUserId, userId)
                .orderByDesc(OrderEntity::getId)
                .last("LIMIT " + size));
        return list.stream()
                .map(o -> new OrderListItemDto(
                        o.getOrderNo(),
                        o.getGoodsId(),
                        o.getPointsCost(),
                        o.getStatus(),
                        o.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }

    public OrderDetailDto getMyOrderDetail(Long userId, String orderNo) {
        if (userId == null || orderNo == null || orderNo.isBlank()) {
            throw new BizException("订单参数不完整");
        }
        OrderEntity order = orderMapper.selectOne(new LambdaQueryWrapper<OrderEntity>()
                .eq(OrderEntity::getUserId, userId)
                .eq(OrderEntity::getOrderNo, orderNo)
                .last("LIMIT 1"));
        if (order == null) {
            throw new BizException("订单不存在");
        }
        List<OrderStatusLogDto> logs = orderStatusLogMapper.selectList(new LambdaQueryWrapper<OrderStatusLogEntity>()
                        .eq(OrderStatusLogEntity::getOrderNo, orderNo)
                        .orderByAsc(OrderStatusLogEntity::getId))
                .stream()
                .map(l -> new OrderStatusLogDto(
                        l.getFromStatus(),
                        l.getToStatus(),
                        l.getReason(),
                        l.getCreatedAt()
                ))
                .collect(Collectors.toList());
        return new OrderDetailDto(
                order.getOrderNo(),
                order.getGoodsId(),
                order.getPointsCost(),
                order.getStatus(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                logs
        );
    }
}
