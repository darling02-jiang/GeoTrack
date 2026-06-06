package com.geotrack.poi.service;

import com.geotrack.common.api.ApiResponse;
import com.geotrack.common.exception.BizException;
import com.geotrack.poi.client.AuthClient;
import feign.FeignException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AuthPointsGrantService {

    private final AuthClient authClient;

    @Value("${geotrack.internal.service-token:dev-internal-token}")
    private String internalServiceToken;

    public AuthPointsGrantService(AuthClient authClient) {
        this.authClient = authClient;
    }

    public void grantCheckInPoints(Long userId, Long checkInRecordId, int rewardPoints) {
        if (rewardPoints <= 0) {
            return;
        }
        Map<String, Object> payload = Map.of(
                "userId", userId,
                "checkInRecordId", checkInRecordId,
                "points", rewardPoints
        );
        try {
            ApiResponse<String> response = authClient.grantCheckInPoints(internalServiceToken, payload);
            if (response == null) {
                throw new BizException("认证服务积分接口返回为空");
            }
            if (response.code() != 0) {
                throw new BizException(response.message() != null ? response.message() : "积分发放失败");
            }
        } catch (FeignException e) {
            throw new BizException("积分发放请求失败，请确认认证服务可用: " + e.getMessage());
        }
    }
}
