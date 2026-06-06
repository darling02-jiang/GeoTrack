package com.geotrack.mall.service;

import com.geotrack.common.api.ApiResponse;
import com.geotrack.common.exception.BizException;
import com.geotrack.mall.client.AuthClient;
import feign.FeignException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AuthPointsDeductService {

    private final AuthClient authClient;

    @Value("${geotrack.internal.service-token:dev-internal-token}")
    private String internalServiceToken;

    public AuthPointsDeductService(AuthClient authClient) {
        this.authClient = authClient;
    }

    public void deductMallOrderPoints(Long userId, String orderNo, int points) {
        Map<String, Object> payload = Map.of(
                "userId", userId,
                "orderNo", orderNo,
                "points", points
        );
        try {
            ApiResponse<String> response = authClient.deductMallPoints(internalServiceToken, payload);
            if (response == null) {
                throw new BizException("认证服务积分扣减返回为空");
            }
            if (response.code() != 0) {
                throw new BizException(response.message() != null ? response.message() : "积分扣减失败");
            }
        } catch (FeignException e) {
            throw new BizException("积分扣减请求失败，请确认认证服务可用: " + e.getMessage());
        }
    }
}
