package com.community.center.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.center.common.PageResult;
import com.community.center.common.Result;
import com.community.center.entity.HealthRecord;
import com.community.center.service.HealthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/health-records")
public class HealthRecordController {

    @Autowired
    private HealthService healthService;

    /**
     * 获取健康记录列表
     */
    @GetMapping
    public Result<PageResult<HealthRecord>> getHealthRecordList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam Map<String, Object> params) {
        Page<HealthRecord> healthRecordPage = healthService.getHealthRecordPage(page, size, params);
        PageResult<HealthRecord> pageResult = PageResult.success(healthRecordPage);
        return Result.success(pageResult);
    }

    /**
     * 获取健康记录详情
     */
    @GetMapping("/{id}")
    public Result<HealthRecord> getHealthRecordById(@PathVariable Long id) {
        HealthRecord healthRecord = healthService.getHealthRecordById(id);
        return Result.success(healthRecord);
    }

    /**
     * 创建健康记录
     */
    @PostMapping
    public Result<Boolean> createHealthRecord(@RequestBody HealthRecord healthRecord) {
        boolean result = healthService.createHealthRecord(healthRecord);
        return Result.success(result);
    }

    /**
     * 更新健康记录
     */
    @PutMapping("/{id}")
    public Result<Boolean> updateHealthRecord(@PathVariable Long id, @RequestBody HealthRecord healthRecord) {
        boolean result = healthService.updateHealthRecord(id, healthRecord);
        return Result.success(result);
    }

    /**
     * 删除健康记录
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteHealthRecord(@PathVariable Long id) {
        boolean result = healthService.deleteHealthRecord(id);
        return Result.success(result);
    }

    /**
     * 根据用户ID获取健康记录列表
     */
    @GetMapping("/user/{userId}")
    public Result<List<HealthRecord>> getHealthRecordsByUserId(@PathVariable Long userId, @RequestParam Map<String, Object> params) {
        List<HealthRecord> healthRecords = healthService.getHealthRecordsByUserId(userId, params);
        return Result.success(healthRecords);
    }
}