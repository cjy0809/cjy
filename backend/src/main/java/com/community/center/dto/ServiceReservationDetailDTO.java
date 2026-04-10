package com.community.center.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 服务预约详情DTO
 * 包含服务详情的预约信息
 */
@Data
public class ServiceReservationDetailDTO {
    
    private Long id;
    
    private Long serviceId;
    
    private Long userId;
    
    private LocalDate reservationDate;
    
    private String reservationTime;
    
    private Integer status; // 预约状态：0-待确认，1-已确认，2-服务中，3-已完成，4-已取消
    
    private String remark;
    
    private String servicePerson;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
    
    // 服务详情字段
    private String serviceName;
    
    private String serviceDescription;
    
    private BigDecimal servicePrice;
    
    private Integer serviceDuration;
    
    private String serviceCategoryName;
    
    private String serviceImageUrl;
}