package com.community.center.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.center.dto.CommentStatisticsDTO;
import com.community.center.dto.RatingStatisticsDTO;
import com.community.center.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 评论Mapper接口
 */
@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
    
    /**
     * 分页查询评论列表（包含用户信息）
     */
    Page<Comment> selectCommentPageWithUserInfo(Page<Comment> page, @Param("targetType") String targetType, @Param("targetId") Long targetId);
    
    /**
     * 分页查询评分列表（包含用户信息）
     */
    Page<Comment> selectRatingPageWithUserInfo(Page<Comment> page, @Param("targetType") String targetType, @Param("rating") Integer rating);
    
    /**
     * 查询评论详情（包含用户信息和回复数）
     */
    Comment selectCommentWithUserInfo(@Param("id") Long id);
    
    /**
     * 查询一级评论列表（包含用户信息和回复数）
     */
    List<Comment> selectFirstLevelComments(@Param("targetType") String targetType, @Param("targetId") Long targetId);
    
    /**
     * 分页查询一级评论列表（包含用户信息和回复数）
     */
    List<Comment> selectFirstLevelCommentsPage(@Param("targetType") String targetType, @Param("targetId") Long targetId, @Param("offset") int offset, @Param("pageSize") int pageSize);
    
    /**
     * 查询子评论列表（包含用户信息）
     */
    List<Comment> selectChildComments(@Param("parentId") Long parentId);
    
    /**
     * 获取评论统计信息
     */
    CommentStatisticsDTO getCommentStatistics();
    
    /**
     * 获取评分统计信息
     */
    RatingStatisticsDTO getRatingStatistics();
    
    /**
     * 根据目标类型获取评分统计信息
     */
    RatingStatisticsDTO getRatingStatisticsByTargetType(@Param("targetType") String targetType);
    
    /**
     * 根据目标类型统计评论数量
     */
    Long countByTargetType(@Param("targetType") String targetType);
    
    /**
     * 统计指定时间范围内的评论数量
     */
    Long countByTimeRange(@Param("startTime") String startTime, @Param("endTime") String endTime);
}