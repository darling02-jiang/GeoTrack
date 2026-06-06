package com.geotrack.poi.client;

import com.geotrack.common.api.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

@FeignClient(name = "geotrack-auth-service", path = "/api/auth")
public interface AuthClient {

    @GetMapping("/token/resolve")
    ApiResponse<Long> resolveToken(@RequestHeader("Cookie") String cookieHeader);

    @PostMapping("/internal/checkin-points")
    ApiResponse<String> grantCheckInPoints(
            @RequestHeader("X-GeoTrack-Internal-Token") String internalServiceToken,
            @RequestBody Map<String, Object> request
    );
}
