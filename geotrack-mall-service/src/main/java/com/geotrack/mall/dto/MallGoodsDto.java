package com.geotrack.mall.dto;

import java.time.LocalDateTime;

public record MallGoodsDto(
        Long id,
        String name,
        Integer pointsPrice,
        Integer stock,
        boolean seckill,
        LocalDateTime beginTime,
        LocalDateTime endTime
) {
}
