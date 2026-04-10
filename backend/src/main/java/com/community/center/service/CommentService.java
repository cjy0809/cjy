package com.community.center.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.center.dto.CommentStatisticsDTO;
import com.community.center.dto.RatingStatisticsDTO;
import com.community.center.dto.request.CommentCreateRequest;
import com.community.center.dto.response.CommentResponse;
import com.community.center.dto.response.RatingResponse;
import com.community.center.entity.Comment;

import java.util.List;

/**
 * 评论服务接口
 */
public interface CommentService {
    
    /**
     * 创建评论
     */
    Long createComment(CommentCreateRequest request, Long userId);
    
    /**
     * 删除评论（逻辑删除）
     */
    void deleteComment(Long commentId, Long userId);
    
    /**
     * 管理员删除评论
     */
    void adminDeleteComment(Long commentId);
    
    /**
     * 分页查询评论列表
     */
    Page<CommentResponse> getCommentPage(int pageNum, int pageSize, String targetType, Long targetId);
    
    /**
     * 获取评论详情
     */
    CommentResponse getCommentById(Long commentId);
    
    /**
     * 获取指定目标的评论列表（包含回复）
     */
    List<CommentResponse> getCommentsByTarget(String targetType, Long targetId, int pageNum, int pageSize);
    
    /**
     * 获取评论统计信息
     */
    CommentStatisticsDTO getCommentStatistics();
    
    /**
     * 分页查询评分列表
     */
    Page<RatingResponse> getRatingPage(int pageNum, int pageSize, String targetType, Integer rating);
    
    /**
     * 获取评分统计信息
     */
    RatingStatisticsDTO getRatingStatistics();
    
    /**
     * 根据目标类型获取评分统计信息
     */
    RatingStatisticsDTO getRatingStatisticsByTargetType(String targetType);
    
    /**
     * 转换评论实体为响应DTO
     */
    CommentResponse convertToResponse(Comment comment);
    
    /**
     * 转换评论实体为评分响应DTO
     */
    RatingResponse convertToRatingResponse(Comment comment);
}