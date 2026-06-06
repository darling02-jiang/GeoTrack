package com.geotrack.order.dto;

import java.time.LocalDateTime;

public record OrderStatusLogDto(
        String fromStatus,
        String toStatus,
        String reason,
        LocalDateTime createdAt
) {
}
