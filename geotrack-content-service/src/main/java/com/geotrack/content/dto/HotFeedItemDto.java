package com.geotrack.content.dto;

import java.time.LocalDateTime;

/** 热门榜单条目 */
public record HotFeedItemDto(
        Long id,
        Long userId,
        Long poiId,
        String content,
        String imageUrl,
        Integer likeCount,
        Integer commentCount,
        Long hotScore,
        LocalDateTime createdAt
) {
}
