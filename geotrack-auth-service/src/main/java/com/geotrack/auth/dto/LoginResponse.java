package com.geotrack.auth.dto;

public record LoginResponse(Long userId, String nickname, String phone) {
}
