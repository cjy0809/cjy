package com.community.center.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户收藏实体类
 * 用于存储用户收藏的活动或服务信息
 */
@Data
@TableName("user_favorite")
public class UserFavorite {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    private String targetType;
    
    private Long targetId;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(exist = false)
    private String targetTitle;
    
    @TableField(exist = false)
    private String targetDescription;
    
    @TableField(exist = false)
    private String targetImage;
}
