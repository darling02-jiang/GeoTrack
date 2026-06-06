package com.geotrack.order.dto;

import java.time.LocalDateTime;

public record OrderListItemDto(
        String orderNo,
        Long goodsId,
        Integer pointsCost,
        String status,
        LocalDateTime createdAt
) {
}
