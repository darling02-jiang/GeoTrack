package com.geotrack.content.controller;

import com.geotrack.common.api.ApiResponse;
import com.geotrack.content.dto.CommentCreateRequest;
import com.geotrack.content.dto.CommentViewDto;
import com.geotrack.content.service.AuthIdentityService;
import com.geotrack.content.service.FeedInteractionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/comment")
@Tag(name = "评论", description = "动态评论发布")
public class CommentController {

    private final FeedInteractionService feedInteractionService;
    private final AuthIdentityService authIdentityService;

    public CommentController(FeedInteractionService feedInteractionService, AuthIdentityService authIdentityService) {
        this.feedInteractionService = feedInteractionService;
        this.authIdentityService = authIdentityService;
    }

    @PostMapping
    @Operation(summary = "发布评论", description = "需要登录 Cookie。")
    public ApiResponse<CommentViewDto> create(
            @RequestHeader(value = "Cookie", required = false) String cookieHeader,
            @RequestBody CommentCreateRequest request
    ) {
        Long userId = authIdentityService.resolveUserId(cookieHeader);
        return ApiResponse.success(feedInteractionService.addComment(userId, request));
    }
}
