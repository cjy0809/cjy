package com.community.center.dto.response;

import lombok.Data;

/**
 * 报名统计响应DTO
 */
@Data
public class RegistrationStatisticsResponse {
    
    private Integer totalRegistrations;
    
    private Integer pendingRegistrations;
    
    private Integer approvedRegistrations;
    
    private Integer rejectedRegistrations;
    
    private Integer cancelledRegistrations;
    
    private Integer thisMonthRegistrations;
}