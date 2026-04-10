package com.community.center.common;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

import java.util.List;

/**
 * 分页结果类
 */
@Data
public class PageResult<T> {
    private Integer code;
    private String message;
    private Long total;
    private List<T> records;

    public PageResult(Integer code, String message, Long total, List<T> records) {
        this.code = code;
        this.message = message;
        this.total = total;
        this.records = records;
    }
    
    /**
     * 从MyBatis Plus的Page对象创建分页结果
     */
    public static <T> PageResult<T> success(Page<T> page) {
        return new PageResult<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), page.getTotal(), page.getRecords());
    }
}