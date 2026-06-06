package com.geotrack.content.dto;

import java.time.LocalDateTime;

public record CommentViewDto(Long id, Long userId, String content, LocalDateTime createdAt) {
}
