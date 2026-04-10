package com.community.center.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 新闻实体类
 * 用于存储社区中心发布的新闻资讯
 */
@Data
@TableName("news")
public class News {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String title;
    
    private String content;
    
    private Integer type;
    
    @TableField("publisher_id")
    private Long publisherId;
    
    @TableField("publish_time")
    private LocalDateTime publishTime;
    
    private Integer status;
    
    @TableField("view_count")
    private Integer viewCount;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
