package com.community.center.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 服务预约实体类
 * 用于存储用户对社区服务的预约信息
 */
@Data
@TableName("service_reservation")
public class ServiceReservation {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long serviceId;
    
    private Long userId;
    
    private LocalDate reservationDate;
    
    private String reservationTime;
    
    private Integer status;
    
    private String remark;
    
    private String servicePerson;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
