package com.community.center.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 活动实体类
 * 用于存储社区中心活动的基本信息
 */
@Data
@TableName("activity")
public class Activity {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String title;
    
    private String content;
    
    @TableField(exist = false)
    private String coverImage;
    
    private Long organizerId;
    
    private String location;
    
    private Integer maxParticipants;
    
    private Integer currentParticipants;
    
    private LocalDateTime startTime;
    
    private LocalDateTime endTime;
    
    private Integer status;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
