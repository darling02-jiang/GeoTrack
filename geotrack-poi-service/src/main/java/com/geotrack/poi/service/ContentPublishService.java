package com.geotrack.poi.service;

import com.geotrack.common.api.ApiResponse;
import com.geotrack.common.exception.BizException;
import com.geotrack.poi.client.ContentClient;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ContentPublishService {

    private final ContentClient contentClient;

    public ContentPublishService(ContentClient contentClient) {
        this.contentClient = contentClient;
    }

    public void createFeed(Long userId, Long poiId, String content, String imageUrl) {
        Map<String, Object> payload = Map.of(
                "userId", userId,
                "poiId", poiId,
                "content", content,
                "imageUrl", imageUrl
        );
        ApiResponse<Long> response = contentClient.createFromCheckIn(payload);
        if (response == null) {
            throw new BizException("内容服务返回为空");
        }
        if (response.code() != 0) {
            throw new BizException(response.message() == null ? "发布失败" : response.message());
        }
    }
}
