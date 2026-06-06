package com.geotrack.poi.controller;

import com.geotrack.common.api.ApiResponse;
import com.geotrack.common.guard.InterfaceGuardService;
import com.geotrack.poi.dto.CheckInMySummaryDto;
import com.geotrack.poi.dto.CheckInRecentItemDto;
import com.geotrack.poi.dto.CheckInRequest;
import com.geotrack.poi.service.AuthIdentityService;
import com.geotrack.poi.service.PoiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.time.Duration;

@RestController
@RequestMapping("/api/checkin")
@Tag(name = "打卡", description = "打卡提交、月度汇总和最近打卡记录")
public class CheckInController {

    private static final String RESOURCE_CHECKIN_SUBMIT = "checkin.submit";

    private final PoiService poiService;
    private final AuthIdentityService authIdentityService;
    private final InterfaceGuardService interfaceGuardService;

    @Value("${geotrack.guard.checkin.qps:30}")
    private double checkInQps;

    @Value("${geotrack.guard.checkin.user-window-seconds:10}")
    private long checkInUserWindowSeconds;

    @Value("${geotrack.guard.checkin.user-max-requests:3}")
    private int checkInUserMaxRequests;

    @Value("${geotrack.guard.checkin.token-ttl-seconds:300}")
    private long checkInTokenTtlSeconds;

    public CheckInController(
            PoiService poiService,
            AuthIdentityService authIdentityService,
            InterfaceGuardService interfaceGuardService
    ) {
        this.poiService = poiService;
        this.authIdentityService = authIdentityService;
        this.interfaceGuardService = interfaceGuardService;
    }

    @PostMapping
    @Operation(summary = "提交打卡", description = "校验登录态、幂等键、用户频率、Redis GEO 距离和当日唯一打卡，成功后投递 RocketMQ。")
    public ApiResponse<String> checkIn(
            @RequestHeader(value = "Cookie", required = false) String cookieHeader,
            @RequestHeader(value = "X-Idempotency-Key", required = false) String idempotencyKey,
            @RequestBody CheckInRequest request
    ) {
        Long userId = authIdentityService.resolveUserId(cookieHeader);
        return ApiResponse.success(interfaceGuardService.protect(
                RESOURCE_CHECKIN_SUBMIT,
                checkInQps,
                userId,
                idempotencyKey,
                Duration.ofSeconds(checkInTokenTtlSeconds),
                Duration.ofSeconds(checkInUserWindowSeconds),
                checkInUserMaxRequests,
                () -> poiService.checkIn(userId, request)
        ));
    }

    @GetMapping("/my-summary")
    @Operation(summary = "我的月度打卡汇总")
    public ApiResponse<CheckInMySummaryDto> mySummary(
            @RequestHeader(value = "Cookie", required = false) String cookieHeader,
            @RequestParam("year") int year,
            @RequestParam("month") int month
    ) {
        Long userId = authIdentityService.resolveUserId(cookieHeader);
        return ApiResponse.success(poiService.getMyCheckInSummary(userId, year, month));
    }

    @GetMapping("/my-recent")
    @Operation(summary = "我的最近打卡记录")
    public ApiResponse<List<CheckInRecentItemDto>> myRecent(
            @RequestHeader(value = "Cookie", required = false) String cookieHeader,
            @RequestParam(name = "limit", defaultValue = "50") int limit
    ) {
        Long userId = authIdentityService.resolveUserId(cookieHeader);
        return ApiResponse.success(poiService.listMyRecentCheckIns(userId, limit));
    }
}
