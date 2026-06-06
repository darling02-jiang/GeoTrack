package com.geotrack.content.client;

import com.geotrack.common.api.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "geotrack-auth-service", path = "/api/auth")
public interface AuthClient {

    @GetMapping("/token/resolve")
    ApiResponse<Long> resolveToken(@RequestHeader("Cookie") String cookieHeader);
}
