package com.geotrack.content.dto;

import java.time.LocalDateTime;
import java.util.List;

/** POI 圈子动态列表项：含若干最新评论预览 */
public record FeedPoiItemDto(
        Long id,
        Long userId,
        Long poiId,
        String content,
        String imageUrl,
        Integer likeCount,
        Integer commentCount,
        Long hotScore,
        LocalDateTime createdAt,
        List<CommentViewDto> commentPreview
) {
}
