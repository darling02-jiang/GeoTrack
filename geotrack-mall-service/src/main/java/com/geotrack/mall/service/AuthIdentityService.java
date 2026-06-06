package com.geotrack.mall.service;

import com.geotrack.common.api.ApiResponse;
import com.geotrack.common.exception.BizException;
import com.geotrack.mall.client.AuthClient;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class AuthIdentityService {

    private final AuthClient authClient;

    public AuthIdentityService(AuthClient authClient) {
        this.authClient = authClient;
    }

    public Long resolveUserId(String cookieHeader) {
        if (!StringUtils.hasText(cookieHeader)) {
            throw new BizException("缺少认证信息");
        }

        ApiResponse<Long> response = authClient.resolveToken(cookieHeader);
        if (response == null) {
            throw new BizException("认证服务返回为空");
        }
        if (response.code() != 0) {
            throw new BizException(response.message() == null ? "认证失败" : response.message());
        }
        Long data = response.data();
        if (data == null) {
            throw new BizException("认证结果异常");
        }
        return data;
    }
}
