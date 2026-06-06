package com.geotrack.auth.service;

import com.geotrack.auth.entity.PointFlowEntity;
import com.geotrack.auth.entity.UserEntity;
import com.geotrack.auth.mapper.PointFlowMapper;
import com.geotrack.auth.mapper.UserMapper;
import com.geotrack.common.exception.BizException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PointsServiceTest {

    private final UserMapper userMapper = mock(UserMapper.class);
    private final PointFlowMapper pointFlowMapper = mock(PointFlowMapper.class);
    private final PointsService pointsService = new PointsService(userMapper, pointFlowMapper);

    @Test
    void grantCheckInPointsIsIdempotentWhenFlowExists() {
        UserEntity locked = new UserEntity();
        locked.setId(1L);
        locked.setPointsBalance(10);
        when(userMapper.selectOne(any())).thenReturn(locked);
        when(pointFlowMapper.selectCount(any())).thenReturn(1L);

        pointsService.grantCheckInPoints(1L, 100L, 5);

        verify(userMapper, never()).updateById(any(UserEntity.class));
        verify(pointFlowMapper, never()).insert(any(PointFlowEntity.class));
    }

    @Test
    void deductMallOrderPointsFailsWhenBalanceIsNotEnough() {
        UserEntity locked = new UserEntity();
        locked.setId(1L);
        locked.setPointsBalance(3);
        when(userMapper.selectOne(any())).thenReturn(locked);
        when(pointFlowMapper.selectCount(any())).thenReturn(0L);

        assertThrows(BizException.class, () -> pointsService.deductMallOrderPoints(1L, "GT001", 10));

        verify(userMapper, never()).updateById(any(UserEntity.class));
        verify(pointFlowMapper, never()).insert(any(PointFlowEntity.class));
    }

    @Test
    void deductMallOrderPointsWritesFlowAndUpdatesBalance() {
        UserEntity locked = new UserEntity();
        locked.setId(1L);
        locked.setPointsBalance(30);
        when(userMapper.selectOne(any())).thenReturn(locked);
        when(pointFlowMapper.selectCount(any())).thenReturn(0L);

        pointsService.deductMallOrderPoints(1L, "GT001", 10);

        verify(userMapper).updateById(any(UserEntity.class));
        verify(pointFlowMapper).insert(any(PointFlowEntity.class));
    }
}
