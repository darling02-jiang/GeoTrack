package com.geotrack.poi.dto;

public record CheckInRecentItemDto(long id, long userId, long poiId, String result, String createdAt) {
}
