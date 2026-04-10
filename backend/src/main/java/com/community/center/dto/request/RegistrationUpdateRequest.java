package com.community.center.dto.request;

import lombok.Data;

/**
 * 更新报名请求DTO
 */
@Data
public class RegistrationUpdateRequest {
    
    private Integer status;
    
    private String remark;
}