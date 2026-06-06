package com.geotrack.auth.controller;

import com.geotrack.auth.dto.LoginRequest;
import com.geotrack.auth.dto.LoginResponse;
import com.geotrack.auth.dto.SendCodeRequest;
import com.geotrack.auth.entity.UserEntity;
import com.geotrack.auth.service.AuthService;
import com.geotrack.common.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "用户与认证", description = "验证码登录、当前用户、会话解析和健康检查")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/code/send")
    @Operation(summary = "发送登录验证码", description = "创建或复用 Session，并向手机号发送验证码；本地开发默认使用 mock 验证码。")
    public ApiResponse<String> sendCode(@RequestBody SendCodeRequest request, HttpSession session) {
        return ApiResponse.success(authService.sendCode(request, session));
    }

    @PostMapping("/login")
    @Operation(summary = "手机号验证码登录", description = "校验 Session 中的验证码，登录成功后写入 Redis Session。")
    public ApiResponse<LoginResponse> login(@RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        HttpSession session = httpRequest.getSession(false);
        if (session == null) {
            return ApiResponse.fail("请先获取验证码；若已发送仍失败，请固定使用 localhost 或 127.0.0.1 其一访问前端，并允许 Cookie");
        }
        return ApiResponse.success(authService.login(request, session));
    }

    @GetMapping("/me")
    @Operation(summary = "获取当前登录用户", description = "根据浏览器 SESSION Cookie 查询当前用户信息。")
    public ApiResponse<UserEntity> me(HttpServletRequest request) {
        UserEntity user = authService.findLoggedInUser(request.getSession(false));
        if (user == null) {
            return ApiResponse.fail("未登录");
        }
        return ApiResponse.success(user);
    }

    @GetMapping("/token/resolve")
    @Operation(summary = "解析当前会话用户 ID", description = "内部服务通过 OpenFeign 转发 Cookie 后调用，用于解析当前登录用户。")
    public ApiResponse<Long> resolveToken() {
        return ApiResponse.success(authService.currentUserId());
    }

    @GetMapping("/health")
    @Operation(summary = "认证服务健康检查")
    public ApiResponse<String> health() {
        return ApiResponse.success("auth-service-ok");
    }
}
