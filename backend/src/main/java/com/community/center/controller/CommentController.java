package com.community.center.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.center.common.PageResult;
import com.community.center.common.Result;
import com.community.center.common.SecurityUtil;
import com.community.center.dto.CommentStatisticsDTO;
import com.community.center.dto.RatingStatisticsDTO;
import com.community.center.dto.request.CommentCreateRequest;
import com.community.center.dto.response.CommentResponse;
import com.community.center.dto.response.RatingResponse;
import com.community.center.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 评论控制器
 * 处理用户评论的创建、删除、查询等操作
 */
@Tag(name = "评论管理")
@RestController
@RequestMapping("/api/comment")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Operation(summary = "创建评论")
    @PostMapping
    public Result<Long> createComment(@Valid @RequestBody CommentCreateRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        Long commentId = commentService.createComment(request, userId);
        return Result.success(commentId);
    }

    @Operation(summary = "删除评论")
    @DeleteMapping("/{commentId}")
    public Result<Void> deleteComment(
            @Parameter(description = "评论ID", required = true) @PathVariable Long commentId) {
        Long userId = SecurityUtil.getCurrentUserId();
        commentService.deleteComment(commentId, userId);
        return Result.success();
    }

    @Operation(summary = "管理员删除评论")
    @DeleteMapping("/admin/{commentId}")
    public Result<Void> adminDeleteComment(
            @Parameter(description = "评论ID", required = true) @PathVariable Long commentId) {
        commentService.adminDeleteComment(commentId);
        return Result.success();
    }

    @Operation(summary = "分页查询评论列表")
    @GetMapping("/page")
    public PageResult<CommentResponse> getCommentPage(
            @Parameter(description = "页码", required = true) @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "每页大小", required = true) @RequestParam(defaultValue = "10") int pageSize,
            @Parameter(description = "评论目标类型") @RequestParam(required = false) String targetType,
            @Parameter(description = "评论目标ID") @RequestParam(required = false) Long targetId) {
        Page<CommentResponse> page = commentService.getCommentPage(pageNum, pageSize, targetType, targetId);
        return PageResult.success(page);
    }

    @Operation(summary = "获取评论详情")
    @GetMapping("/{commentId}")
    public Result<CommentResponse> getCommentById(
            @Parameter(description = "评论ID", required = true) @PathVariable Long commentId) {
        CommentResponse comment = commentService.getCommentById(commentId);
        return Result.success(comment);
    }

    @Operation(summary = "获取指定目标的评论列表")
    @GetMapping("/target")
    public Result<List<CommentResponse>> getCommentsByTarget(
            @Parameter(description = "评论目标类型", required = true) @RequestParam String targetType,
            @Parameter(description = "评论目标ID", required = true) @RequestParam Long targetId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int pageSize) {
        List<CommentResponse> comments = commentService.getCommentsByTarget(targetType, targetId, pageNum, pageSize);
        return Result.success(comments);
    }

    @Operation(summary = "获取评论统计信息")
    @GetMapping("/statistics")
    public Result<CommentStatisticsDTO> getCommentStatistics() {
        CommentStatisticsDTO statistics = commentService.getCommentStatistics();
        return Result.success(statistics);
    }

    @Operation(summary = "分页查询评分列表")
    @GetMapping("/rating/page")
    public PageResult<RatingResponse> getRatingPage(
            @Parameter(description = "页码", required = true) @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "每页大小", required = true) @RequestParam(defaultValue = "10") int pageSize,
            @Parameter(description = "评论目标类型") @RequestParam(required = false) String targetType,
            @Parameter(description = "评分等级") @RequestParam(required = false) Integer rating) {
        Page<RatingResponse> page = commentService.getRatingPage(pageNum, pageSize, targetType, rating);
        return PageResult.success(page);
    }

    @Operation(summary = "获取评分统计信息")
    @GetMapping("/rating/statistics")
    public Result<RatingStatisticsDTO> getRatingStatistics() {
        RatingStatisticsDTO statistics = commentService.getRatingStatistics();
        return Result.success(statistics);
    }

    @Operation(summary = "根据目标类型获取评分统计信息")
    @GetMapping("/rating/statistics/{targetType}")
    public Result<RatingStatisticsDTO> getRatingStatisticsByTargetType(
            @Parameter(description = "评论目标类型", required = true) @PathVariable String targetType) {
        RatingStatisticsDTO statistics = commentService.getRatingStatisticsByTargetType(targetType);
        return Result.success(statistics);
    }
}
