package com.community.center.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.center.common.SecurityUtil;
import com.community.center.dto.CommentStatisticsDTO;
import com.community.center.dto.RatingStatisticsDTO;
import com.community.center.dto.request.CommentCreateRequest;
import com.community.center.dto.response.CommentResponse;
import com.community.center.dto.response.RatingResponse;
import com.community.center.entity.Comment;
import com.community.center.exception.BusinessException;
import com.community.center.mapper.CommentMapper;
import com.community.center.service.CommentService;
import com.community.center.util.ContentFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 评论服务实现类
 */
@Slf4j
@Service
public class CommentServiceImpl implements CommentService {
    
    @Autowired
    private CommentMapper commentMapper;
    
    @Override
    @Transactional
    public Long createComment(CommentCreateRequest request, Long userId) {
        try {
            // 验证用户ID
            if (userId == null) {
                throw new BusinessException("用户未登录，请先登录");
            }
            
            // 验证评论内容
            if (request.getContent() == null || request.getContent().trim().isEmpty()) {
                throw new BusinessException("评论内容不能为空");
            }
            
            // 内容过滤
            ContentFilter.ContentCheckResult checkResult = ContentFilter.checkContent(request.getContent());
            if (!checkResult.isSafe()) {
                throw new BusinessException(checkResult.getMessage());
            }
            
            // 验证目标类型
            if (!isValidTargetType(request.getTargetType())) {
                throw new BusinessException("无效的评论目标类型");
            }
            
            // 验证目标ID
            if (request.getTargetId() == null || request.getTargetId() <= 0) {
                throw new BusinessException("无效的评论目标ID");
            }
            
            // 如果是回复评论，验证父评论是否存在
            if (request.getParentId() != null && request.getParentId() > 0) {
                Comment parentComment = commentMapper.selectById(request.getParentId());
                if (parentComment == null) {
                    throw new BusinessException("父评论不存在");
                }
                if (parentComment.getStatus() == 1) {
                    throw new BusinessException("父评论已被删除");
                }
            }
            
            Comment comment = new Comment();
            BeanUtils.copyProperties(request, comment);
            comment.setUserId(userId);
            comment.setParentId(request.getParentId() != null ? request.getParentId() : 0L);
            comment.setStatus(0); // 0: 正常，1: 删除
            comment.setRating(request.getRating()); // 设置评分
            
            commentMapper.insert(comment);
            
            return comment.getId();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("创建评论失败，请稍后重试");
        }
    }
    
    @Override
    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException("评论不存在");
        }
        
        // 只有评论作者或管理员可以删除评论
        if (!comment.getUserId().equals(userId) && !SecurityUtil.isAdmin()) {
            throw new BusinessException("无权限删除此评论");
        }
        
        // 物理删除评论
        commentMapper.deleteById(commentId);
        
        // 如果是一级评论，同时删除所有子评论
        if (comment.getParentId() == 0) {
            QueryWrapper<Comment> wrapper = new QueryWrapper<>();
            wrapper.eq("parent_id", commentId);
            
            List<Comment> childComments = commentMapper.selectList(wrapper);
            for (Comment child : childComments) {
                commentMapper.deleteById(child.getId());
            }
        }
    }
    
    @Override
    @Transactional
    public void adminDeleteComment(Long commentId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException("评论不存在");
        }
        
        // 管理员物理删除
        commentMapper.deleteById(commentId);
        
        // 如果是一级评论，同时删除所有子评论
        if (comment.getParentId() == 0) {
            QueryWrapper<Comment> wrapper = new QueryWrapper<>();
            wrapper.eq("parent_id", commentId);
            
            List<Comment> childComments = commentMapper.selectList(wrapper);
            for (Comment child : childComments) {
                commentMapper.deleteById(child.getId());
            }
        }
    }
    
    @Override
    public Page<CommentResponse> getCommentPage(int pageNum, int pageSize, String targetType, Long targetId) {
        Page<Comment> page = new Page<>(pageNum, pageSize);
        Page<Comment> commentPage = commentMapper.selectCommentPageWithUserInfo(page, targetType, targetId);
        
        Page<CommentResponse> responsePage = new Page<>();
        BeanUtils.copyProperties(commentPage, responsePage);
        
        List<CommentResponse> responseList = commentPage.getRecords().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        responsePage.setRecords(responseList);
        
        return responsePage;
    }
    
    @Override
    public CommentResponse getCommentById(Long commentId) {
        Comment comment = commentMapper.selectCommentWithUserInfo(commentId);
        if (comment == null) {
            throw new BusinessException("评论不存在");
        }
        
        CommentResponse response = convertToResponse(comment);
        
        // 如果是一级评论，获取子评论
        if (comment.getParentId() == 0) {
            List<Comment> childComments = commentMapper.selectChildComments(commentId);
            List<CommentResponse> childResponses = childComments.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
            response.setChildComments(childResponses);
        }
        
        return response;
    }
    
    @Override
    public List<CommentResponse> getCommentsByTarget(String targetType, Long targetId, int pageNum, int pageSize) {
        // 计算偏移量
        int offset = (pageNum - 1) * pageSize;
        
        List<CommentResponse> result = new ArrayList<>();
        
        // 获取一级评论（分页）
        List<Comment> firstLevelComments = commentMapper.selectFirstLevelCommentsPage(targetType, targetId, offset, pageSize);
        
        for (Comment comment : firstLevelComments) {
            CommentResponse response = convertToResponse(comment);
            
            // 获取子评论
            List<Comment> childComments = commentMapper.selectChildComments(comment.getId());
            List<CommentResponse> childResponses = childComments.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
            
            response.setChildComments(childResponses);
            result.add(response);
        }
        
        return result;
    }
    
    @Override
    public CommentStatisticsDTO getCommentStatistics() {
        return commentMapper.getCommentStatistics();
    }
    
    @Override
    public Page<RatingResponse> getRatingPage(int pageNum, int pageSize, String targetType, Integer rating) {
        Page<Comment> page = new Page<>(pageNum, pageSize);
        
        // 使用带有用户信息的查询方法
        Page<Comment> commentPage = commentMapper.selectRatingPageWithUserInfo(page, targetType, rating);
        
        // 转换为RatingResponse
        Page<RatingResponse> responsePage = new Page<>();
        BeanUtils.copyProperties(commentPage, responsePage);
        
        List<RatingResponse> responseList = commentPage.getRecords().stream()
                .map(this::convertToRatingResponse)
                .collect(Collectors.toList());
        responsePage.setRecords(responseList);
        
        return responsePage;
    }
    
    @Override
    public RatingStatisticsDTO getRatingStatistics() {
        RatingStatisticsDTO statistics = commentMapper.getRatingStatistics();
        return statistics;
    }
    
    @Override
    public RatingStatisticsDTO getRatingStatisticsByTargetType(String targetType) {
        return commentMapper.getRatingStatisticsByTargetType(targetType);
    }
    
    @Override
    public CommentResponse convertToResponse(Comment comment) {
        CommentResponse response = new CommentResponse();
        BeanUtils.copyProperties(comment, response);
        return response;
    }
    
    @Override
    public RatingResponse convertToRatingResponse(Comment comment) {
        RatingResponse response = new RatingResponse();
        BeanUtils.copyProperties(comment, response);
        return response;
    }
    
    /**
     * 验证目标类型是否有效
     */
    private boolean isValidTargetType(String targetType) {
        return "activity".equals(targetType) || 
               "news".equals(targetType) ||
               "service_item".equals(targetType);
    }
}