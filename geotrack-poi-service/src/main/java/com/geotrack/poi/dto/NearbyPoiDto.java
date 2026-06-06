package com.geotrack.poi.dto;

public record NearbyPoiDto(
        Long id,
        String name,
        double distanceMeters,
        double longitude,
        double latitude,
        int radiusMeters,
        int rewardPoints,
        int status,
        String description
) {
}
