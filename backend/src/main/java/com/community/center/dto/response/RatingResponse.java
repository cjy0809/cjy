package com.community.center.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评分响应DTO
 */
@Data
public class RatingResponse {
    
    /**
     * 评论ID
     */
    private Long id;
    
    /**
     * 评论内容
     */
    private String content;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 用户名
     */
    private String userName;
    
    /**
     * 用户头像
     */
    private String userAvatar;
    
    /**
     * 用户角色
     */
    private String userRole;
    
    /**
     * 评论目标类型
     */
    private String targetType;
    
    /**
     * 评论目标ID
     */
    private Long targetId;
    
    /**
     * 评论目标标题
     */
    private String targetTitle;
    
    /**
     * 父评论ID
     */
    private Long parentId;
    
    /**
     * 评论状态
     */
    private Integer status;
    
    /**
     * 评分
     */
    private Integer rating;
    
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}