package com.geotrack.poi.client;

import com.geotrack.common.api.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "geotrack-content-service", path = "/api/feed")
public interface ContentClient {

    @PostMapping("/internal/create")
    ApiResponse<Long> createFromCheckIn(@RequestBody Map<String, Object> request);
}
