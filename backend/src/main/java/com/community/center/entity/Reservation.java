package com.community.center.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 场地预约实体类
 * 用于存储用户对社区中心场地的预约信息
 */
@Data
@TableName("venue_reservation")
public class Reservation {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long venueId;
    
    private Long userId;
    
    private LocalDate reservationDate;
    
    private LocalTime startTime;
    
    private LocalTime endTime;
    
    private String purpose;
    
    private Integer participants;
    
    private Integer status;
    
    private Long reviewerId;
    
    private LocalDateTime reviewTime;
    
    private String reviewComment;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
