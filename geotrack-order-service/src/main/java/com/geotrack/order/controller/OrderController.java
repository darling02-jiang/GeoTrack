package com.geotrack.order.controller;

import com.geotrack.common.api.ApiResponse;
import com.geotrack.order.dto.OrderDetailDto;
import com.geotrack.order.dto.OrderListItemDto;
import com.geotrack.order.service.AuthIdentityService;
import com.geotrack.order.service.OrderQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/order")
@Tag(name = "订单查询", description = "订单健康检查和我的订单列表")
public class OrderController {

    private final OrderQueryService orderQueryService;
    private final AuthIdentityService authIdentityService;

    public OrderController(OrderQueryService orderQueryService, AuthIdentityService authIdentityService) {
        this.orderQueryService = orderQueryService;
        this.authIdentityService = authIdentityService;
    }

    @GetMapping("/health")
    @Operation(summary = "订单服务健康检查")
    public ApiResponse<String> health() {
        return ApiResponse.success("order-service-ok");
    }

    @GetMapping("/my")
    @Operation(summary = "查询我的订单列表", description = "需要登录 Cookie，按创建时间倒序返回。")
    public ApiResponse<List<OrderListItemDto>> myOrders(
            @RequestHeader(value = "Cookie", required = false) String cookieHeader,
            @RequestParam(name = "limit", defaultValue = "50") int limit
    ) {
        Long userId = authIdentityService.resolveUserId(cookieHeader);
        return ApiResponse.success(orderQueryService.listMyOrders(userId, limit));
    }

    @GetMapping("/{orderNo}")
    @Operation(summary = "查询订单详情", description = "需要登录 Cookie，返回订单基本信息和状态流转记录。")
    public ApiResponse<OrderDetailDto> detail(
            @RequestHeader(value = "Cookie", required = false) String cookieHeader,
            @PathVariable("orderNo") String orderNo
    ) {
        Long userId = authIdentityService.resolveUserId(cookieHeader);
        return ApiResponse.success(orderQueryService.getMyOrderDetail(userId, orderNo));
    }
}
