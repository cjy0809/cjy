package com.community.center.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 活动报名实体类
 * 用于存储用户参与活动的报名信息
 */
@Data
@TableName("activity_registration")
public class Registration {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    private Long activityId;
    
    private LocalDateTime registrationTime;
    
    private Integer status;
    
    private Integer registrationType;
    
    private Long operatorId;
    
    @TableField(exist = false)
    private String remark;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
