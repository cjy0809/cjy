package com.community.center.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.center.entity.HealthRecord;

import java.util.List;
import java.util.Map;

public interface HealthService {
    /**
     * 根据ID查询健康记录
     * @param id 健康记录ID
     * @return 健康记录对象
     */
    HealthRecord getHealthRecordById(Long id);

    /**
     * 查询健康记录列表
     * @param params 查询参数
     * @return 健康记录列表
     */
    List<HealthRecord> getHealthRecordList(Map<String, Object> params);

    /**
     * 分页查询健康记录列表
     * @param page 页码
     * @param size 每页大小
     * @param params 查询参数
     * @return 健康记录分页对象
     */
    Page<HealthRecord> getHealthRecordPage(Integer page, Integer size, Map<String, Object> params);

    /**
     * 创建健康记录
     * @param healthRecord 健康记录对象
     * @return 创建结果
     */
    boolean createHealthRecord(HealthRecord healthRecord);

    /**
     * 更新健康记录
     * @param id 健康记录ID
     * @param healthRecord 健康记录对象
     * @return 更新结果
     */
    boolean updateHealthRecord(Long id, HealthRecord healthRecord);

    /**
     * 删除健康记录
     * @param id 健康记录ID
     * @return 删除结果
     */
    boolean deleteHealthRecord(Long id);

    /**
     * 根据用户ID查询健康记录列表
     * @param userId 用户ID
     * @param params 查询参数
     * @return 健康记录列表
     */
    List<HealthRecord> getHealthRecordsByUserId(Long userId, Map<String, Object> params);
    
    /**
     * 根据用户ID删除该用户的所有健康记录
     * @param userId 用户ID
     * @return 删除结果
     */
    boolean deleteHealthRecordsByUserId(Long userId);
}