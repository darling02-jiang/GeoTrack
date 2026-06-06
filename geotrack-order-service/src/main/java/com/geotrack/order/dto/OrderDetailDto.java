package com.geotrack.order.dto;

import java.time.LocalDateTime;
import java.util.List;

public record OrderDetailDto(
        String orderNo,
        Long goodsId,
        Integer pointsCost,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<OrderStatusLogDto> statusLogs
) {
}
