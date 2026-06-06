package com.geotrack.mall.controller;

import com.geotrack.common.api.ApiResponse;
import com.geotrack.common.guard.InterfaceGuardService;
import com.geotrack.mall.dto.OrderResultDto;
import com.geotrack.mall.dto.PlaceOrderRequest;
import com.geotrack.mall.service.AuthIdentityService;
import com.geotrack.mall.service.SeckillOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

/** 秒杀入口：逻辑见 {@link com.geotrack.mall.service.SeckillOrderService}、库存见 {@link com.geotrack.mall.service.SeckillRedisStockService} */
@RestController
@RequestMapping("/api/seckill")
@Tag(name = "秒杀", description = "Redis Lua 秒杀下单入口")
public class SeckillController {

    private static final String RESOURCE_SECKILL_ORDER = "seckill.order";

    private final SeckillOrderService seckillOrderService;
    private final AuthIdentityService authIdentityService;
    private final InterfaceGuardService interfaceGuardService;

    @Value("${geotrack.guard.seckill.qps:100}")
    private double seckillQps;

    @Value("${geotrack.guard.seckill.user-window-seconds:10}")
    private long seckillUserWindowSeconds;

    @Value("${geotrack.guard.seckill.user-max-requests:3}")
    private int seckillUserMaxRequests;

    @Value("${geotrack.guard.seckill.token-ttl-seconds:600}")
    private long seckillTokenTtlSeconds;

    public SeckillController(
            SeckillOrderService seckillOrderService,
            AuthIdentityService authIdentityService,
            InterfaceGuardService interfaceGuardService
    ) {
        this.seckillOrderService = seckillOrderService;
        this.authIdentityService = authIdentityService;
        this.interfaceGuardService = interfaceGuardService;
    }

    /**
     * 秒杀下单：Redis Lua 原子扣库存 + 单用户限购；成功后扣积分并落库订单。
     * 需请求头：Cookie、X-Idempotency-Key。
     */
    @PostMapping("/order")
    @Operation(summary = "秒杀下单", description = "Redis Lua 原子预扣库存和限购标记，成功后投递 RocketMQ 异步创建订单和扣积分。")
    public ApiResponse<OrderResultDto> place(
            @RequestHeader(value = "Cookie", required = false) String cookieHeader,
            @RequestHeader(value = "X-Idempotency-Key", required = false) String idempotencyKey,
            @RequestBody PlaceOrderRequest request
    ) {
        Long userId = authIdentityService.resolveUserId(cookieHeader);
        Long goodsId = request == null ? null : request.goodsId();
        return ApiResponse.success(interfaceGuardService.protect(
                RESOURCE_SECKILL_ORDER,
                seckillQps,
                userId,
                idempotencyKey,
                Duration.ofSeconds(seckillTokenTtlSeconds),
                Duration.ofSeconds(seckillUserWindowSeconds),
                seckillUserMaxRequests,
                () -> seckillOrderService.place(userId, goodsId, idempotencyKey)
        ));
    }
}
