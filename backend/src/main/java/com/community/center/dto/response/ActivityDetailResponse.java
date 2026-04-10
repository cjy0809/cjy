package com.community.center.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 活动详情响应DTO
 */
@Data
public class ActivityDetailResponse {
    
    private Long id;
    
    private String title;
    
    private String content;
    
    private String coverImage;
    
    private Long organizerId;
    
    private String location;
    
    private Integer maxParticipants;
    
    private Integer currentParticipants;
    
    private LocalDateTime startTime;
    
    private LocalDateTime endTime;
    
    private Integer status;
    
    private Integer registrationStatus;
    
    private LocalDateTime createTime;
    
    private Boolean isRegistered;
    
    // 组织者信息
    private String organizerName;
    
    private String organizerTitle;
    
    private String organizerPhone;
    
    private String organizerAvatar;
}