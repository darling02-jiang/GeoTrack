package com.geotrack.content.support;

import com.geotrack.content.entity.FeedEntity;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * 热门值：点赞、评论加权 + 发布时间衰减（需求 4.4.4），供榜单按 hot_score 排序。
 */
public final class FeedHotScoreCalculator {

    private FeedHotScoreCalculator() {
    }

    public static long compute(FeedEntity f) {
        int likes = f.getLikeCount() == null ? 0 : f.getLikeCount();
        int comments = f.getCommentCount() == null ? 0 : f.getCommentCount();
        LocalDateTime created = f.getCreatedAt();
        long hours = created == null ? 0L : ChronoUnit.HOURS.between(created, LocalDateTime.now());
        long decay = Math.min(hours * 200L, 4_000_000L);
        return (long) likes * 10_000L + (long) comments * 5_000L + 8_000_000L - decay;
    }
}
