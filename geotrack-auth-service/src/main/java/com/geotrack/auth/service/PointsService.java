package com.geotrack.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.geotrack.auth.entity.PointFlowEntity;
import com.geotrack.auth.entity.UserEntity;
import com.geotrack.auth.mapper.PointFlowMapper;
import com.geotrack.auth.mapper.UserMapper;
import com.geotrack.common.exception.BizException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PointsService {

    private static final String BIZ_TYPE_CHECKIN = "CHECKIN";
    private static final String BIZ_TYPE_MALL_ORDER = "MALL_ORDER";

    private final UserMapper userMapper;
    private final PointFlowMapper pointFlowMapper;

    public PointsService(UserMapper userMapper, PointFlowMapper pointFlowMapper) {
        this.userMapper = userMapper;
        this.pointFlowMapper = pointFlowMapper;
    }

    /**
     * 打卡成功后的积分发放；以打卡记录 id 为幂等键，重复调用不会重复加积分。
     */
    @Transactional(rollbackFor = Exception.class)
    public void grantCheckInPoints(Long userId, Long checkInRecordId, int points) {
        if (userId == null || checkInRecordId == null) {
            throw new BizException("积分发放参数不完整");
        }
        if (points <= 0) {
            return;
        }
        String bizNo = String.valueOf(checkInRecordId);
        // 与积分流水唯一键配合：先锁用户行再查流水，避免并发双写
        UserEntity locked = userMapper.selectOne(new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getId, userId)
                .last("FOR UPDATE"));
        if (locked == null) {
            throw new BizException("用户不存在，积分发放失败");
        }
        Long existed = pointFlowMapper.selectCount(new LambdaQueryWrapper<PointFlowEntity>()
                .eq(PointFlowEntity::getUserId, userId)
                .eq(PointFlowEntity::getBizType, BIZ_TYPE_CHECKIN)
                .eq(PointFlowEntity::getBizNo, bizNo));
        if (existed != null && existed > 0) {
            return;
        }
        int before = locked.getPointsBalance() == null ? 0 : locked.getPointsBalance();
        int after = before + points;
        locked.setPointsBalance(after);
        userMapper.updateById(locked);

        PointFlowEntity flow = new PointFlowEntity();
        flow.setUserId(userId);
        flow.setBizType(BIZ_TYPE_CHECKIN);
        flow.setBizNo(bizNo);
        flow.setChangeAmount(points);
        flow.setBalanceBefore(before);
        flow.setBalanceAfter(after);
        pointFlowMapper.insert(flow);
    }

    /**
     * 商城下单扣减积分；以订单号为幂等键，重复调用不会重复扣减。
     */
    @Transactional(rollbackFor = Exception.class)
    public void deductMallOrderPoints(Long userId, String orderNo, int points) {
        if (userId == null || orderNo == null || orderNo.isBlank()) {
            throw new BizException("积分扣减参数不完整");
        }
        if (points <= 0) {
            throw new BizException("扣减积分必须大于 0");
        }
        // 行锁 + 流水幂等：同一 orderNo 重复扣减直接返回（依赖 gt_point_flow 唯一索引）
        UserEntity locked = userMapper.selectOne(new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getId, userId)
                .last("FOR UPDATE"));
        if (locked == null) {
            throw new BizException("用户不存在，积分扣减失败");
        }
        Long existed = pointFlowMapper.selectCount(new LambdaQueryWrapper<PointFlowEntity>()
                .eq(PointFlowEntity::getUserId, userId)
                .eq(PointFlowEntity::getBizType, BIZ_TYPE_MALL_ORDER)
                .eq(PointFlowEntity::getBizNo, orderNo));
        if (existed != null && existed > 0) {
            return;
        }
        int before = locked.getPointsBalance() == null ? 0 : locked.getPointsBalance();
        if (before < points) {
            throw new BizException("积分不足");
        }
        int after = before - points;
        locked.setPointsBalance(after);
        userMapper.updateById(locked);

        PointFlowEntity flow = new PointFlowEntity();
        flow.setUserId(userId);
        flow.setBizType(BIZ_TYPE_MALL_ORDER);
        flow.setBizNo(orderNo);
        flow.setChangeAmount(-points);
        flow.setBalanceBefore(before);
        flow.setBalanceAfter(after);
        pointFlowMapper.insert(flow);
    }
}
