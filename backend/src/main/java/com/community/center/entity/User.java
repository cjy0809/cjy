package com.community.center.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体类
 * 用于存储社区中心用户的基本信息
 */
@Data
@TableName("user")
public class User {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String username;
    
    private String password;
    
    private String phone;
    
    private String name;
    
    private Integer age;
    
    private Integer gender;
    
    private String avatar;
    
    private java.math.BigDecimal balance;
    
    private String emergencyContact;
    
    private String emergencyPhone;
    
    private String healthStatus;
    
    private String address;
    
    private String role;
    
    private Integer status;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
