package com.community.center.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户充值实体类
 * 用于存储用户的账户充值记录
 */
@Data
@TableName("recharge")
public class Recharge {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    private BigDecimal amount;
    
    private Integer status;
    
    private String paymentMethod;
    
    private String transactionId;
    
    private String orderNo;
    
    private LocalDateTime payTime;
    
    private String remark;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableLogic
    private Integer isDeleted;
}
