package com.geotrack.content.dto;

public record FeedCreateRequest(
        Long checkInRecordId,
        Long userId,
        Long poiId,
        String content,
        String imageUrl
) {
}
