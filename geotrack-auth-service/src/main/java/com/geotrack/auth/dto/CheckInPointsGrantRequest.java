package com.geotrack.auth.dto;

public record CheckInPointsGrantRequest(Long userId, Long checkInRecordId, Integer points) {
}
