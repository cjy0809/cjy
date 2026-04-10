package com.community.center.dto.request;

import lombok.Data;

import javax.validation.constraints.Positive;
import java.time.LocalDateTime;

/**
 * 活动更新请求DTO
 */
@Data
public class ActivityUpdateRequest {
    
    private String title;
    
    private String content;
    
    private String coverImage;
    
    private Long organizerId;
    
    private String location;
    
    @Positive(message = "最大参与人数必须大于0")
    private Integer maxParticipants;
    
    private LocalDateTime startTime;
    
    private LocalDateTime endTime;
    
    private Integer status;
    
    private Integer registrationStatus;
    
    
}