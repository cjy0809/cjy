package com.community.center.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 报名列表响应DTO
 */
@Data
public class RegistrationListResponse {
    
    private Long id;
    
    private Long userId;
    
    private Long activityId;
    
    private LocalDateTime registrationTime;
    
    private Integer status;
    
    private String remark;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
    
    // 活动信息
    private String activityTitle;
    
    private LocalDateTime activityStartTime;
    
    private LocalDateTime activityEndTime;
    
    // 用户信息
    private String userName;
    
    private String userPhone;
}