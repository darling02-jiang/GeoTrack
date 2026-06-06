package com.geotrack.content.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.geotrack.common.exception.BizException;
import com.geotrack.content.dto.CommentViewDto;
import com.geotrack.content.dto.FeedCreateRequest;
import com.geotrack.content.dto.FeedPoiItemDto;
import com.geotrack.content.dto.HotFeedItemDto;
import com.geotrack.content.entity.CommentEntity;
import com.geotrack.content.entity.FeedEntity;
import com.geotrack.content.mapper.CommentMapper;
import com.geotrack.content.mapper.FeedMapper;
import com.geotrack.content.support.FeedHotScoreCalculator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FeedService {

    private static final int PREVIEW_COMMENTS = 5;

    private final FeedMapper feedMapper;
    private final CommentMapper commentMapper;

    public FeedService(FeedMapper feedMapper, CommentMapper commentMapper) {
        this.feedMapper = feedMapper;
        this.commentMapper = commentMapper;
    }

    public List<FeedEntity> listRecent(int limit) {
        int size = limit <= 0 ? 30 : Math.min(limit, 100);
        return feedMapper.selectList(new LambdaQueryWrapper<FeedEntity>()
                .orderByDesc(FeedEntity::getCreatedAt)
                .last("limit " + size));
    }

    /**
     * 指定 POI 圈子动态；sort=latest 按时间，sort=likes 按点赞数（同分再按时间）。
     */
    public List<FeedPoiItemDto> listFeedsForPoi(long poiId, String sort, int limit) {
        int size = limit <= 0 ? 30 : Math.min(limit, 100);
        LambdaQueryWrapper<FeedEntity> q = new LambdaQueryWrapper<FeedEntity>()
                .eq(FeedEntity::getPoiId, poiId);
        if ("likes".equalsIgnoreCase(sort)) {
            q.orderByDesc(FeedEntity::getLikeCount).orderByDesc(FeedEntity::getCreatedAt);
        } else {
            q.orderByDesc(FeedEntity::getCreatedAt);
        }
        q.last("limit " + size);
        List<FeedEntity> feeds = feedMapper.selectList(q);
        if (feeds.isEmpty()) {
            return List.of();
        }
        List<Long> ids = feeds.stream().map(FeedEntity::getId).collect(Collectors.toList());
        Map<Long, List<CommentViewDto>> previews = loadCommentPreviews(ids, PREVIEW_COMMENTS);
        return feeds.stream()
                .map(f -> toPoiItem(f, previews.getOrDefault(f.getId(), List.of())))
                .collect(Collectors.toList());
    }

    /**
     * 热门榜单：按 hot_score 降序；poiId 为空时为全站，否则仅该 POI 圈子。
     */
    public List<HotFeedItemDto> listHotFeeds(Long poiId, int limit) {
        int size = Math.min(Math.max(limit, 1), 100);
        LambdaQueryWrapper<FeedEntity> q = new LambdaQueryWrapper<>();
        if (poiId != null) {
            q.eq(FeedEntity::getPoiId, poiId);
        }
        q.orderByDesc(FeedEntity::getHotScore).orderByDesc(FeedEntity::getCreatedAt).last("limit " + size);
        return feedMapper.selectList(q).stream().map(this::toHotItem).collect(Collectors.toList());
    }

    @Transactional(rollbackFor = Exception.class)
    public Long createFromCheckIn(FeedCreateRequest request) {
        if (request == null || request.userId() == null || request.poiId() == null) {
            throw new BizException("发帖参数不完整");
        }
        if (request.checkInRecordId() != null) {
            FeedEntity existed = feedMapper.selectOne(new LambdaQueryWrapper<FeedEntity>()
                    .eq(FeedEntity::getCheckInRecordId, request.checkInRecordId())
                    .last("limit 1"));
            if (existed != null) {
                return existed.getId();
            }
        }
        if (!StringUtils.hasText(request.content())) {
            throw new BizException("帖子文字不能为空");
        }
        if (!StringUtils.hasText(request.imageUrl())) {
            throw new BizException("帖子图片不能为空");
        }
        if (request.content().length() > 500) {
            throw new BizException("帖子文字长度不能超过 500 字");
        }
        if (request.imageUrl().length() > 255) {
            throw new BizException("图片地址长度不能超过 255 字符");
        }

        FeedEntity feed = new FeedEntity();
        feed.setCheckInRecordId(request.checkInRecordId());
        feed.setUserId(request.userId());
        feed.setPoiId(request.poiId());
        feed.setContent(request.content().trim());
        feed.setImageUrl(request.imageUrl().trim());
        feed.setLikeCount(0);
        feed.setCommentCount(0);
        feed.setHotScore(0L);
        feedMapper.insert(feed);

        FeedEntity reloaded = feedMapper.selectById(feed.getId());
        if (reloaded != null) {
            reloaded.setHotScore(FeedHotScoreCalculator.compute(reloaded));
            feedMapper.updateById(reloaded);
        }
        return feed.getId();
    }

    private FeedPoiItemDto toPoiItem(FeedEntity f, List<CommentViewDto> preview) {
        return new FeedPoiItemDto(
                f.getId(),
                f.getUserId(),
                f.getPoiId(),
                f.getContent(),
                f.getImageUrl(),
                f.getLikeCount(),
                f.getCommentCount(),
                f.getHotScore(),
                f.getCreatedAt(),
                preview
        );
    }

    private HotFeedItemDto toHotItem(FeedEntity f) {
        return new HotFeedItemDto(
                f.getId(),
                f.getUserId(),
                f.getPoiId(),
                f.getContent(),
                f.getImageUrl(),
                f.getLikeCount(),
                f.getCommentCount(),
                f.getHotScore(),
                f.getCreatedAt()
        );
    }

    private Map<Long, List<CommentViewDto>> loadCommentPreviews(List<Long> feedIds, int each) {
        Map<Long, List<CommentViewDto>> out = new LinkedHashMap<>();
        if (feedIds.isEmpty()) {
            return out;
        }
        List<CommentEntity> all = commentMapper.selectList(new LambdaQueryWrapper<CommentEntity>()
                .in(CommentEntity::getFeedId, feedIds)
                .orderByDesc(CommentEntity::getCreatedAt));
        Map<Long, List<CommentEntity>> grouped = new LinkedHashMap<>();
        for (CommentEntity c : all) {
            grouped.computeIfAbsent(c.getFeedId(), k -> new ArrayList<>()).add(c);
        }
        for (Map.Entry<Long, List<CommentEntity>> e : grouped.entrySet()) {
            List<CommentViewDto> slice = e.getValue().stream()
                    .limit(each)
                    .map(c -> new CommentViewDto(c.getId(), c.getUserId(), c.getContent(), c.getCreatedAt()))
                    .collect(Collectors.toList());
            Collections.reverse(slice);
            out.put(e.getKey(), slice);
        }
        return out;
    }
}
