package com.geotrack.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.geotrack.order.entity.OrderEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderMapper extends BaseMapper<OrderEntity> {
}
