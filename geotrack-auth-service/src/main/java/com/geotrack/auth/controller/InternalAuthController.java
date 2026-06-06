package com.geotrack.auth.controller;

import com.geotrack.auth.dto.CheckInPointsGrantRequest;
import com.geotrack.auth.dto.MallPointsDeductRequest;
import com.geotrack.auth.service.PointsService;
import com.geotrack.common.api.ApiResponse;
import com.geotrack.common.exception.BizException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 服务间调用：需 Header {@code X-GeoTrack-Internal-Token}，供 POI/商城等服务发积分、扣积分，勿对浏览器暴露。
 */
@RestController
@RequestMapping("/api/auth/internal")
@Tag(name = "认证内部接口", description = "服务间积分发放和扣减接口，需要内部调用 Token")
public class InternalAuthController {

    private final PointsService pointsService;

    @Value("${geotrack.internal.service-token:dev-internal-token}")
    private String expectedServiceToken;

    public InternalAuthController(PointsService pointsService) {
        this.pointsService = pointsService;
    }

    @PostMapping("/checkin-points")
    @Operation(summary = "发放打卡积分", description = "POI 打卡成功后由消息消费或内部调用触发，按 checkInRecordId 幂等。")
    public ApiResponse<String> grantCheckInPoints(
            @RequestHeader(value = "X-GeoTrack-Internal-Token", required = false) String token,
            @RequestBody CheckInPointsGrantRequest request
    ) {
        assertInternalToken(token);
        if (request == null || request.userId() == null || request.checkInRecordId() == null) {
            throw new BizException("请求体不完整");
        }
        int pts = request.points() == null ? 0 : request.points();
        pointsService.grantCheckInPoints(request.userId(), request.checkInRecordId(), pts);
        return ApiResponse.success("积分发放成功");
    }

    @PostMapping("/mall-points-deduct")
    @Operation(summary = "扣减商城订单积分", description = "商城下单后按 orderNo 幂等扣减用户积分。")
    public ApiResponse<String> deductMallPoints(
            @RequestHeader(value = "X-GeoTrack-Internal-Token", required = false) String token,
            @RequestBody MallPointsDeductRequest request
    ) {
        assertInternalToken(token);
        if (request == null || request.userId() == null || request.orderNo() == null || request.orderNo().isBlank()) {
            throw new BizException("请求体不完整");
        }
        int pts = request.points() == null ? 0 : request.points();
        pointsService.deductMallOrderPoints(request.userId(), request.orderNo(), pts);
        return ApiResponse.success("积分扣减成功");
    }

    private void assertInternalToken(String token) {
        if (!StringUtils.hasText(token) || !expectedServiceToken.equals(token)) {
            throw new BizException("内部调用未授权");
        }
    }
}
