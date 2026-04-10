package com.community.center.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付记录实体类
 * 用于存储用户的支付信息
 */
@Data
@TableName("payment")
public class Payment {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long bookingId;
    
    private Long serviceId;
    
    private Long userId;
    
    private String paymentMethod;
    
    private BigDecimal amount;
    
    private String transactionId;
    
    private Integer status;
    
    private String remark;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
