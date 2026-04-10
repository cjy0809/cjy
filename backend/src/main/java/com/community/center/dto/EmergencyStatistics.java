package com.community.center.dto;

import lombok.Data;

/**
 * 紧急求助统计数据DTO
 */
@Data
public class EmergencyStatistics {

    /**
     * 今日求助数量
     */
    private Integer todayCount;

    /**
     * 待处理数量
     */
    private Integer pendingCount;

    /**
     * 已处理数量
     */
    private Integer resolvedCount;

    /**
     * 平均响应时间（分钟）
     */
    private Double avgResponseTime;
}