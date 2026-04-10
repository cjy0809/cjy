package com.community.center.dto;

import lombok.Data;

/**
 * 分页查询参数
 */
@Data
public class PageQueryDTO {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private String keyword;
    private Long categoryId;
    private Long serviceId;
    private Long userId;
    private String userName;
    private Integer status;
    private Integer minRating;
}
