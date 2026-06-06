package com.geotrack.mall.controller;

import com.geotrack.common.api.ApiResponse;
import com.geotrack.mall.dto.MallGoodsDto;
import com.geotrack.mall.dto.OrderResultDto;
import com.geotrack.mall.dto.PlaceOrderRequest;
import com.geotrack.mall.service.AuthIdentityService;
import com.geotrack.mall.service.MallCatalogService;
import com.geotrack.mall.service.MallExchangeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 积分商城：商品目录 + 普通兑换（非秒杀），需登录 Cookie 与幂等键 */
@RestController
@RequestMapping("/api/mall")
@Tag(name = "积分商城", description = "商品目录、商品详情和普通积分兑换")
public class MallController {

    private final MallCatalogService mallCatalogService;
    private final MallExchangeService mallExchangeService;
    private final AuthIdentityService authIdentityService;

    public MallController(
            MallCatalogService mallCatalogService,
            MallExchangeService mallExchangeService,
            AuthIdentityService authIdentityService
    ) {
        this.mallCatalogService = mallCatalogService;
        this.mallExchangeService = mallExchangeService;
        this.authIdentityService = authIdentityService;
    }

    @GetMapping("/goods")
    @Operation(summary = "查询上架商品列表")
    public ApiResponse<List<MallGoodsDto>> listGoods() {
        return ApiResponse.success(mallCatalogService.listOnShelf());
    }

    @GetMapping("/goods/{id}")
    @Operation(summary = "查询商品详情")
    public ApiResponse<MallGoodsDto> goodsDetail(@PathVariable("id") Long id) {
        return ApiResponse.success(mallCatalogService.getDetail(id));
    }

    /**
     * 普通积分兑换（非秒杀商品），需请求头：Cookie（登录会话）、X-Idempotency-Key（幂等键）。
     */
    @PostMapping("/exchange")
    @Operation(summary = "普通积分兑换", description = "非秒杀商品兑换；需要登录 Cookie 和 X-Idempotency-Key。")
    public ApiResponse<OrderResultDto> exchange(
            @RequestHeader(value = "Cookie", required = false) String cookieHeader,
            @RequestHeader(value = "X-Idempotency-Key", required = false) String idempotencyKey,
            @RequestBody PlaceOrderRequest request
    ) {
        Long userId = authIdentityService.resolveUserId(cookieHeader);
        Long goodsId = request == null ? null : request.goodsId();
        return ApiResponse.success(mallExchangeService.exchange(userId, goodsId, idempotencyKey));
    }
}
