package com.geotrack.order.service;

import com.geotrack.common.exception.BizException;
import com.geotrack.order.dto.OrderDetailDto;
import com.geotrack.order.entity.OrderEntity;
import com.geotrack.order.entity.OrderStatusLogEntity;
import com.geotrack.order.mapper.OrderMapper;
import com.geotrack.order.mapper.OrderStatusLogMapper;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OrderQueryServiceTest {

    private final OrderMapper orderMapper = mock(OrderMapper.class);
    private final OrderStatusLogMapper orderStatusLogMapper = mock(OrderStatusLogMapper.class);
    private final OrderQueryService service = new OrderQueryService(orderMapper, orderStatusLogMapper);

    @Test
    void getMyOrderDetailReturnsStatusLogs() {
        OrderEntity order = new OrderEntity();
        order.setOrderNo("GT001");
        order.setUserId(1L);
        order.setGoodsId(2L);
        order.setPointsCost(100);
        order.setStatus("PAID");
        order.setCreatedAt(LocalDateTime.now());

        OrderStatusLogEntity log = new OrderStatusLogEntity();
        log.setFromStatus("PENDING");
        log.setToStatus("PAID");
        log.setReason("积分扣减成功");
        log.setCreatedAt(LocalDateTime.now());

        when(orderMapper.selectOne(any())).thenReturn(order);
        when(orderStatusLogMapper.selectList(any())).thenReturn(List.of(log));

        OrderDetailDto detail = service.getMyOrderDetail(1L, "GT001");

        assertEquals("GT001", detail.orderNo());
        assertEquals("PAID", detail.status());
        assertEquals(1, detail.statusLogs().size());
        assertEquals("PENDING", detail.statusLogs().get(0).fromStatus());
    }

    @Test
    void getMyOrderDetailFailsWhenOrderMissing() {
        when(orderMapper.selectOne(any())).thenReturn(null);

        assertThrows(BizException.class, () -> service.getMyOrderDetail(1L, "GT404"));
    }
}
