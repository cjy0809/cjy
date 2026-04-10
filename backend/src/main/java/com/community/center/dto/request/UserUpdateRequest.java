package com.community.center.dto.request;

import lombok.Data;

import javax.validation.constraints.Pattern;

/**
 * 用户信息更新请求DTO
 */
@Data
public class UserUpdateRequest {
    
    private String name;
    
    private String username;
    
    private String password;
    
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
    
    private String avatar;
    
    private Integer gender;
    
    private Integer age;
    
    private String address;
    
    private Integer status;
    
    private String role;
    
    private String emergencyContact;
    
    private String emergencyPhone;
    
    private String healthStatus;
}