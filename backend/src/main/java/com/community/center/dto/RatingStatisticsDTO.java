package com.community.center.dto;

import lombok.Data;

/**
 * 评分统计DTO
 */
@Data
public class RatingStatisticsDTO {
    
    /**
     * 总评分数
     */
    private Integer totalRatings;
    
    /**
     * 活动评分数
     */
    private Integer activityRatings;
    
    /**
     * 服务评分数
     */
    private Integer serviceItemRatings;
    
    /**
     * 平均评分
     */
    private Double averageRating;
    
    /**
     * 活动平均评分
     */
    private Double activityAverageRating;
    
    /**
     * 服务平均评分
     */
    private Double serviceItemAverageRating;
    
    /**
     * 五星评分数量
     */
    private Integer fiveStarCount;
    
    /**
     * 四星评分数量
     */
    private Integer fourStarCount;
    
    /**
     * 三星评分数量
     */
    private Integer threeStarCount;
    
    /**
     * 二星评分数量
     */
    private Integer twoStarCount;
    
    /**
     * 一星评分数量
     */
    private Integer oneStarCount;
}