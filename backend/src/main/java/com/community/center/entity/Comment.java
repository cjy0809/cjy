package com.community.center.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评论实体类
 * 用于存储用户对活动、服务等对象的评论信息
 */
@Data
@TableName("comment")
public class Comment {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String content;
    
    @TableField("user_id")
    private Long userId;
    
    @TableField("target_type")
    private String targetType;
    
    @TableField("target_id")
    private Long targetId;
    
    @TableField("parent_id")
    private Long parentId;
    
    private Integer status;
    
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
    
    @TableField("rating")
    private Integer rating;
    
    // 非数据库字段
    @TableField(exist = false)
    private String userName;
    
    @TableField(exist = false)
    private String userAvatar;
    
    @TableField(exist = false)
    private String userRole;
    
    @TableField(exist = false)
    private Integer replyCount;
    
    @TableField(exist = false)
    private String targetTitle;
}
