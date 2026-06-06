package com.geotrack.poi.dto;

public record CheckInRequest(
        Long poiId,
        Double longitude,
        Double latitude,
        String content,
        String imageUrl
) {
}
