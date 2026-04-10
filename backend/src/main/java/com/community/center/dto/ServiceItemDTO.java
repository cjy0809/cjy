package com.community.center.dto;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 服务项目数据传输对象
 */
@Data
public class ServiceItemDTO {
    private Long id;
    private Long categoryId;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer duration;
    private Integer status;
    private String categoryName; // 用于显示分类名称
}
