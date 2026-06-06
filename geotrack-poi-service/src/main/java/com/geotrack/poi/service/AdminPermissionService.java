package com.geotrack.poi.service;

import com.geotrack.common.exception.BizException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AdminPermissionService {

    @Value("${geotrack.poi.admin-user-ids:}")
    private String adminUserIds;

    public void assertAdmin(Long userId) {
        if (userId == null) {
            throw new BizException("缺少用户身份");
        }
        if (!resolveAdminIds().contains(userId)) {
            throw new BizException("仅管理员可执行该操作");
        }
    }

    private Set<Long> resolveAdminIds() {
        if (!StringUtils.hasText(adminUserIds)) {
            return Set.of();
        }
        return Arrays.stream(adminUserIds.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .map(Long::valueOf)
                .collect(Collectors.toSet());
    }
}
