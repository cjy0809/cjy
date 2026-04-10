package com.community.center.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 评论响应DTO
 */
@Data
public class CommentResponse {
    
    private Long id;
    
    private String content;
    
    private Long userId;
    
    private String userName;
    
    private String userAvatar;
    
    private String userRole;
    
    private String targetType;
    
    private Long targetId;
    
    private String targetTitle;
    
    private Long parentId;
    
    private Integer status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
    
    private Integer rating;
    
    private Integer replyCount;
    
    private List<CommentResponse> childComments;
}