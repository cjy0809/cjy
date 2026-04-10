package com.community.center.dto.request;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 场地预约请求DTO
 */
@Data
public class ReservationCreateRequest {
    
    @NotNull(message = "用户ID不能为空")
    private Long userId;
    
    @NotNull(message = "场地ID不能为空")
    private Long venueId;
    
    @NotNull(message = "预约日期不能为空")
    private LocalDate reservationDate;
    
    @NotNull(message = "预约开始时间不能为空")
    private LocalTime startTime;
    
    @NotNull(message = "预约结束时间不能为空")
    private LocalTime endTime;
    
    private String purpose;
    
    @NotNull(message = "参与人数不能为空")
    private Integer participants;
}