package com.community.center.dto.response;

import lombok.Data;

/**
 * 用户登录响应DTO
 */
@Data
public class UserLoginResponse {
    
    private String token;
    
    private Long userId;
    
    private String username;
    
    private String name;
    
    private String role;
}