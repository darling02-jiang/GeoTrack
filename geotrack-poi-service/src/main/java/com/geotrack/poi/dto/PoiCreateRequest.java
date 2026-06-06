package com.geotrack.poi.dto;

public record PoiCreateRequest(
        String name,
        Double longitude,
        Double latitude,
        Integer radiusMeters,
        Integer rewardPoints,
        String description,
        Integer status
) {
}
