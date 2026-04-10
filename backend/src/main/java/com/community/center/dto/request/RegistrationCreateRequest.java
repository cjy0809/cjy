package com.community.center.dto.request;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 创建报名请求DTO
 */
@Data
public class RegistrationCreateRequest {
    
    @NotNull(message = "用户ID不能为空")
    private Long userId;
    
    @NotNull(message = "活动ID不能为空")
    private Long activityId;
    
    private String remark;
    
    private Integer registrationType;
    
    private Long operatorId;
}