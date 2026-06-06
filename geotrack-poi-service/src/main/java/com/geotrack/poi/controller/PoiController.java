package com.geotrack.poi.controller;

import com.geotrack.common.api.ApiResponse;
import com.geotrack.poi.dto.NearbyPoiDto;
import com.geotrack.poi.dto.PoiCreateRequest;
import com.geotrack.poi.dto.PoiStatusPatchRequest;
import com.geotrack.poi.entity.PoiEntity;
import com.geotrack.poi.service.AdminPermissionService;
import com.geotrack.poi.service.AuthIdentityService;
import com.geotrack.poi.service.PoiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** POI 管理、Redis GEO 附近查询（公开）、打卡入口在 {@link com.geotrack.poi.controller.CheckInController} */
@RestController
@RequestMapping("/api/poi")
@Tag(name = "POI 管理", description = "POI 创建、列表、附近查询和上下架")
public class PoiController {

    private final PoiService poiService;
    private final AuthIdentityService authIdentityService;
    private final AdminPermissionService adminPermissionService;

    public PoiController(
            PoiService poiService,
            AuthIdentityService authIdentityService,
            AdminPermissionService adminPermissionService
    ) {
        this.poiService = poiService;
        this.authIdentityService = authIdentityService;
        this.adminPermissionService = adminPermissionService;
    }

    @PostMapping
    @Operation(summary = "创建 POI", description = "管理员接口，启用状态的 POI 会同步写入 Redis GEO。")
    public ApiResponse<Long> create(
            @RequestHeader(value = "Cookie", required = false) String cookieHeader,
            @RequestBody PoiCreateRequest request
    ) {
        Long userId = authIdentityService.resolveUserId(cookieHeader);
        adminPermissionService.assertAdmin(userId);
        return ApiResponse.success(poiService.createPoi(request));
    }

    @GetMapping("/list")
    @Operation(summary = "查询 POI 列表")
    public ApiResponse<List<PoiEntity>> list() {
        return ApiResponse.success(poiService.list());
    }

    /**
     * Redis GEO 附近 POI：以给定坐标为圆心，返回半径内 POI 中心点（仅 status=1），按距离升序。
     */
    @GetMapping("/nearby")
    @Operation(summary = "查询附近 POI", description = "基于 Redis GEO 查询指定坐标半径内的启用 POI，并按距离升序返回。")
    public ApiResponse<List<NearbyPoiDto>> nearby(
            @RequestParam("longitude") double longitude,
            @RequestParam("latitude") double latitude,
            @RequestParam(value = "radiusMeters", defaultValue = "3000") double radiusMeters,
            @RequestParam(value = "limit", defaultValue = "20") int limit
    ) {
        return ApiResponse.success(poiService.listNearbyPois(longitude, latitude, radiusMeters, limit));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "更新 POI 状态", description = "管理员接口；启用写入 Redis GEO，停用从 Redis GEO 移除。")
    public ApiResponse<Void> updateStatus(
            @RequestHeader(value = "Cookie", required = false) String cookieHeader,
            @PathVariable("id") Long id,
            @RequestBody PoiStatusPatchRequest request
    ) {
        Long userId = authIdentityService.resolveUserId(cookieHeader);
        adminPermissionService.assertAdmin(userId);
        poiService.updatePoiStatus(id, request == null ? null : request.status());
        return ApiResponse.success(null);
    }
}
