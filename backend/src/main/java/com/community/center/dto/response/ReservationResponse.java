package com.community.center.dto.response;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 预约响应DTO
 */
@Data
public class ReservationResponse {
    
    private Long id;
    
    private Long venueId;
    
    private String venueName;
    
    private Long userId;
    
    private String userName;
    
    private LocalDate reservationDate;
    
    private LocalTime startTime;
    
    private LocalTime endTime;
    
    private String purpose;
    
    private Integer participants;
    
    private Integer status;
    
    private String statusName;
    
    private Long reviewerId;
    
    private String reviewerName;
    
    private LocalDateTime reviewTime;
    
    private String reviewComment;
    
    private LocalDateTime createTime;
}