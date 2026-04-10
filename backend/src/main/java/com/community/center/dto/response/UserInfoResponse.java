package com.community.center.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户信息响应DTO
 */
@Data
public class UserInfoResponse {
    
    private Long id;
    
    private String username;
    
    private String name;
    
    private String phone;
    
    private Integer age;
    
    private String address;
    
    private String avatar;
    
    private Integer gender;
    
    private String role;
    
    private Integer status;
    
    private String emergencyContact;
    
    private String emergencyPhone;
    
    private String healthStatus;
    
    private BigDecimal balance;
    
    private LocalDateTime createTime;
}