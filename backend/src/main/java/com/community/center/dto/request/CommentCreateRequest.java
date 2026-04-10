package com.community.center.dto.request;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 创建评论请求DTO
 */
@Data
public class CommentCreateRequest {
    
    @NotBlank(message = "评论内容不能为空")
    private String content;
    
    @NotNull(message = "评论目标类型不能为空")
    private String targetType;
    
    @NotNull(message = "评论目标ID不能为空")
    private Long targetId;
    
    private Long parentId = 0L;
    
    @Min(value = 1, message = "评分不能低于1星")
    @Max(value = 5, message = "评分不能高于5星")
    private Integer rating;
}