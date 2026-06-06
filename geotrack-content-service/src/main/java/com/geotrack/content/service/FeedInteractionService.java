package com.geotrack.content.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.geotrack.common.exception.BizException;
import com.geotrack.content.dto.CommentCreateRequest;
import com.geotrack.content.dto.CommentViewDto;
import com.geotrack.content.dto.LikeToggleResponse;
import com.geotrack.content.entity.CommentEntity;
import com.geotrack.content.entity.FeedEntity;
import com.geotrack.content.entity.FeedLikeEntity;
import com.geotrack.content.mapper.CommentMapper;
import com.geotrack.content.mapper.FeedLikeMapper;
import com.geotrack.content.mapper.FeedMapper;
import com.geotrack.content.support.FeedHotScoreCalculator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class FeedInteractionService {

    private final FeedMapper feedMapper;
    private final FeedLikeMapper feedLikeMapper;
    private final CommentMapper commentMapper;

    public FeedInteractionService(
            FeedMapper feedMapper,
            FeedLikeMapper feedLikeMapper,
            CommentMapper commentMapper
    ) {
        this.feedMapper = feedMapper;
        this.feedLikeMapper = feedLikeMapper;
        this.commentMapper = commentMapper;
    }

    /** 点赞/取消点赞：同一用户对同一动态仅一条记录，再次调用为取消 */
    @Transactional(rollbackFor = Exception.class)
    public LikeToggleResponse toggleLike(Long userId, Long feedId) {
        if (userId == null || feedId == null) {
            throw new BizException("参数不完整");
        }
        FeedEntity feed = feedMapper.selectOne(new LambdaQueryWrapper<FeedEntity>()
                .eq(FeedEntity::getId, feedId)
                .last("FOR UPDATE"));
        if (feed == null) {
            throw new BizException("动态不存在");
        }
        FeedLikeEntity existed = feedLikeMapper.selectOne(new LambdaQueryWrapper<FeedLikeEntity>()
                .eq(FeedLikeEntity::getFeedId, feedId)
                .eq(FeedLikeEntity::getUserId, userId));
        int likes = feed.getLikeCount() == null ? 0 : feed.getLikeCount();
        boolean nowLiked;
        if (existed != null) {
            feedLikeMapper.deleteById(existed.getId());
            likes = Math.max(0, likes - 1);
            nowLiked = false;
        } else {
            FeedLikeEntity row = new FeedLikeEntity();
            row.setFeedId(feedId);
            row.setUserId(userId);
            feedLikeMapper.insert(row);
            likes = likes + 1;
            nowLiked = true;
        }
        feed.setLikeCount(likes);
        feed.setHotScore(FeedHotScoreCalculator.compute(feed));
        feedMapper.updateById(feed);
        return new LikeToggleResponse(nowLiked, likes);
    }

    @Transactional(rollbackFor = Exception.class)
    public CommentViewDto addComment(Long userId, CommentCreateRequest request) {
        if (userId == null || request == null || request.feedId() == null) {
            throw new BizException("参数不完整");
        }
        if (!StringUtils.hasText(request.content()) || request.content().length() > 500) {
            throw new BizException("评论内容长度须在 1～500 字");
        }
        FeedEntity feed = feedMapper.selectOne(new LambdaQueryWrapper<FeedEntity>()
                .eq(FeedEntity::getId, request.feedId())
                .last("FOR UPDATE"));
        if (feed == null) {
            throw new BizException("动态不存在");
        }
        CommentEntity c = new CommentEntity();
        c.setFeedId(request.feedId());
        c.setUserId(userId);
        c.setContent(request.content().trim());
        commentMapper.insert(c);

        int cc = feed.getCommentCount() == null ? 0 : feed.getCommentCount();
        feed.setCommentCount(cc + 1);
        feed.setHotScore(FeedHotScoreCalculator.compute(feed));
        feedMapper.updateById(feed);

        return new CommentViewDto(c.getId(), c.getUserId(), c.getContent(), c.getCreatedAt());
    }
}
