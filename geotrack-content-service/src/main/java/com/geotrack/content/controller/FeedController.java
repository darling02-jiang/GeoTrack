package com.geotrack.content.controller;

import com.geotrack.common.api.ApiResponse;
import com.geotrack.content.dto.FeedCreateRequest;
import com.geotrack.content.dto.FeedPoiItemDto;
import com.geotrack.content.dto.HotFeedItemDto;
import com.geotrack.content.entity.FeedEntity;
import com.geotrack.content.service.FeedService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/feed")
@Tag(name = "动态", description = "动态时间流、POI 圈子动态、热门榜和内部创建接口")
public class FeedController {

    private final FeedService feedService;

    public FeedController(FeedService feedService) {
        this.feedService = feedService;
    }

    /** 全站最新动态（兼容旧前端） */
    @GetMapping("/recent")
    @Operation(summary = "查询全站最新动态")
    public ApiResponse<List<FeedEntity>> recent(@RequestParam(value = "limit", required = false) Integer limit) {
        return ApiResponse.success(feedService.listRecent(limit == null ? 30 : limit));
    }

    /**
     * POI 圈子动态；sort=latest（默认）按发布时间，sort=likes 按点赞数。
     */
    @GetMapping("/poi/{poiId}")
    @Operation(summary = "查询 POI 圈子动态", description = "sort=latest 按时间排序，sort=likes 按点赞数排序。")
    public ApiResponse<List<FeedPoiItemDto>> feedsByPoi(
            @PathVariable("poiId") long poiId,
            @RequestParam(value = "sort", defaultValue = "latest") String sort,
            @RequestParam(value = "limit", defaultValue = "30") int limit
    ) {
        return ApiResponse.success(feedService.listFeedsForPoi(poiId, sort, limit));
    }

    /**
     * 热门榜单：可选 poiId 限定圈子，按 hot_score 降序。
     */
    @GetMapping("/hot")
    @Operation(summary = "查询热门动态榜", description = "可按 poiId 限定圈子，按 hot_score 降序。")
    public ApiResponse<List<HotFeedItemDto>> hot(
            @RequestParam(value = "poiId", required = false) Long poiId,
            @RequestParam(value = "limit", defaultValue = "10") int limit
    ) {
        return ApiResponse.success(feedService.listHotFeeds(poiId, limit));
    }

    @PostMapping("/internal/create")
    @Operation(summary = "内部创建打卡动态", description = "打卡成功后由 MQ 消费主链路创建，HTTP 接口用于兼容服务间调用。")
    public ApiResponse<Long> createFromCheckIn(@RequestBody FeedCreateRequest request) {
        return ApiResponse.success(feedService.createFromCheckIn(request));
    }
}
