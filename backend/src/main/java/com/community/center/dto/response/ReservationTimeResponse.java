package com.community.center.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 预约时间响应DTO
 */
@Data
public class ReservationTimeResponse {
    
    private Long id;
    
    private LocalDateTime startTime;
    
    private LocalDateTime endTime;
    
    private String purpose;
    
    private Integer status;
    
    private String userName;
}