package com.community.center.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.community.center.common.VenueStatusEnum;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 场地实体类
 * 用于存储社区中心场地的基本信息
 */
@Data
@TableName("venue")
public class Venue {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String name;
    
    private String description;
    
    private Integer capacity;
    
    @TableField("equipment")
    private String equipment;
    
    @TableField("open_time")
    private String openTime;
    
    @TableField("close_time")
    private String closeTime;
    
    private Integer status;
    
    @TableField("maintenance_date")
    private LocalDateTime maintenanceDate;
    
    @TableField("maintenance_start_time")
    private String maintenanceStartTime;
    
    @TableField("maintenance_end_time")
    private String maintenanceEndTime;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    /**
     * 获取场地状态枚举
     * @return 场地状态枚举
     */
    public VenueStatusEnum getStatusEnum() {
        return VenueStatusEnum.getByCode(this.status);
    }
    
    /**
     * 设置场地状态
     * @param statusEnum 场地状态枚举
     */
    public void setStatusEnum(VenueStatusEnum statusEnum) {
        if (statusEnum != null) {
            this.status = statusEnum.getCode();
        }
    }
    
    /**
     * 检查场地在指定日期是否处于维护状态
     * @param date 检查日期
     * @return 是否处于维护状态
     */
    public boolean isUnderMaintenanceOnDate(LocalDateTime date) {
        if (maintenanceDate == null) {
            return false;
        }
        
        // 检查是否是维护日期
        LocalDateTime maintenanceStart = maintenanceDate;
        LocalDateTime maintenanceEnd = maintenanceDate;
        
        // 如果有指定维护时间段，则设置具体时间
        if (maintenanceStartTime != null && maintenanceEndTime != null) {
            String[] startParts = maintenanceStartTime.split(":");
            String[] endParts = maintenanceEndTime.split(":");
            
            if (startParts.length >= 2 && endParts.length >= 2) {
                try {
                    int startHour = Integer.parseInt(startParts[0]);
                    int startMinute = Integer.parseInt(startParts[1]);
                    int endHour = Integer.parseInt(endParts[0]);
                    int endMinute = Integer.parseInt(endParts[1]);
                    
                    maintenanceStart = maintenanceDate.withHour(startHour).withMinute(startMinute).withSecond(0);
                    maintenanceEnd = maintenanceDate.withHour(endHour).withMinute(endMinute).withSecond(0);
                } catch (NumberFormatException e) {
                    // 时间格式解析失败，使用默认值
                    maintenanceStart = maintenanceDate.withHour(0).withMinute(0).withSecond(0);
                    maintenanceEnd = maintenanceDate.withHour(23).withMinute(59).withSecond(59);
                }
            }
        }
        
        return !date.isBefore(maintenanceStart) && !date.isAfter(maintenanceEnd);
    }
}
