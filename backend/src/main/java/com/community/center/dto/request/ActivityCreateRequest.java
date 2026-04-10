package com.community.center.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.time.LocalDateTime;

/**
 * 活动创建请求DTO
 */
@Data
public class ActivityCreateRequest {
    
    @NotBlank(message = "活动标题不能为空")
    private String title;
    
    @NotBlank(message = "活动内容不能为空")
    private String content;
    
    private String coverImage;
    
    @NotBlank(message = "活动地点不能为空")
    private String location;
    
    private Long venueId;
    
    @NotNull(message = "最大参与人数不能为空")
    @Positive(message = "最大参与人数必须大于0")
    private Integer maxParticipants;
    
    @NotNull(message = "活动开始时间不能为空")
    private LocalDateTime startTime;
    
    @NotNull(message = "活动结束时间不能为空")
    private LocalDateTime endTime;
    
    @NotNull(message = "报名状态不能为空")
    private Integer registrationStatus;
    
    private Integer status;
}