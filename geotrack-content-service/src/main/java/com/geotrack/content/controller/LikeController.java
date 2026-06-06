package com.geotrack.content.controller;

import com.geotrack.common.api.ApiResponse;
import com.geotrack.common.guard.InterfaceGuardService;
import com.geotrack.content.dto.LikeToggleRequest;
import com.geotrack.content.dto.LikeToggleResponse;
import com.geotrack.content.service.AuthIdentityService;
import com.geotrack.content.service.FeedInteractionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@RestController
@RequestMapping("/api/like")
@Tag(name = "点赞", description = "动态点赞和取消点赞")
public class LikeController {

    private static final String RESOURCE_LIKE_TOGGLE = "like.toggle";

    private final FeedInteractionService feedInteractionService;
    private final AuthIdentityService authIdentityService;
    private final InterfaceGuardService interfaceGuardService;

    @Value("${geotrack.guard.like.qps:80}")
    private double likeQps;

    @Value("${geotrack.guard.like.user-window-seconds:10}")
    private long likeUserWindowSeconds;

    @Value("${geotrack.guard.like.user-max-requests:20}")
    private int likeUserMaxRequests;

    @Value("${geotrack.guard.like.token-ttl-seconds:60}")
    private long likeTokenTtlSeconds;

    public LikeController(
            FeedInteractionService feedInteractionService,
            AuthIdentityService authIdentityService,
            InterfaceGuardService interfaceGuardService
    ) {
        this.feedInteractionService = feedInteractionService;
        this.authIdentityService = authIdentityService;
        this.interfaceGuardService = interfaceGuardService;
    }

    @PostMapping("/toggle")
    @Operation(summary = "切换点赞状态", description = "需要登录 Cookie 和 X-Idempotency-Key，带用户频率控制和 Sentinel QPS 限流。")
    public ApiResponse<LikeToggleResponse> toggle(
            @RequestHeader(value = "Cookie", required = false) String cookieHeader,
            @RequestHeader(value = "X-Idempotency-Key", required = false) String idempotencyKey,
            @RequestBody LikeToggleRequest request
    ) {
        Long userId = authIdentityService.resolveUserId(cookieHeader);
        long feedId = request == null || request.feedId() == null ? 0L : request.feedId();
        return ApiResponse.success(interfaceGuardService.protect(
                RESOURCE_LIKE_TOGGLE,
                likeQps,
                userId,
                idempotencyKey,
                Duration.ofSeconds(likeTokenTtlSeconds),
                Duration.ofSeconds(likeUserWindowSeconds),
                likeUserMaxRequests,
                () -> feedInteractionService.toggleLike(userId, feedId)
        ));
    }
}
