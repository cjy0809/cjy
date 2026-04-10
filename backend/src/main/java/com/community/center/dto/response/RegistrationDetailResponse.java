package com.community.center.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 报名详情响应DTO
 */
@Data
public class RegistrationDetailResponse {
    
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
    
    private String activityLocation;
    
    // 用户信息
    private String userName;
    
    private String userPhone;
}