package com.community.center.dto;

import lombok.Data;

/**
 * 评论统计DTO
 */
@Data
public class CommentStatisticsDTO {
    
    private Long totalComments;
    
    private Long activityComments;
    
    private Long serviceItemComments;
    
    private Long newsComments;
    
    private Long todayComments;
    
    private Long thisWeekComments;
    
    private Long thisMonthComments;
}