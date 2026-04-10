package com.community.center.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class RechargeRequest {
    
    private Long userId;
    
    private BigDecimal amount;
    
    private String paymentMethod;
    
    private String remark;
}
