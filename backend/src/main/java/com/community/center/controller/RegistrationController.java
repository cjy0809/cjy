package com.community.center.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.center.common.PageResult;
import com.community.center.common.Result;
import com.community.center.common.ResultCode;
import com.community.center.dto.request.BatchApproveRegistrationRequest;
import com.community.center.dto.request.RegistrationCreateRequest;
import com.community.center.dto.request.RegistrationUpdateRequest;
import com.community.center.dto.response.RegistrationDetailResponse;
import com.community.center.dto.response.RegistrationListResponse;
import com.community.center.dto.response.RegistrationStatisticsResponse;
import com.community.center.exception.BusinessException;
import com.community.center.service.RegistrationService;
import com.community.center.mapper.RegistrationMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 活动报名控制器
 */
@Tag(name = "活动报名管理")
@RestController
@RequestMapping("/api/registration")
public class RegistrationController {

    @Autowired
    private RegistrationService registrationService;
    
    @Autowired
    private RegistrationMapper registrationMapper;

    @Operation(summary = "创建报名记录")
    @PostMapping
    public Result<Void> createRegistration(@Valid @RequestBody RegistrationCreateRequest request) {
        registrationService.createRegistration(request);
        return Result.success();
    }

    @Operation(summary = "更新报名记录")
    @PutMapping("/{registrationId}")
    public Result<Void> updateRegistration(
            @Parameter(description = "报名ID", required = true) @PathVariable Long registrationId,
            @Valid @RequestBody RegistrationUpdateRequest request) {
        registrationService.updateRegistration(registrationId, request);
        return Result.success();
    }

    @Operation(summary = "删除报名记录")
    @DeleteMapping("/{registrationId}")
    public Result<Void> deleteRegistration(
            @Parameter(description = "报名ID", required = true) @PathVariable Long registrationId) {
        registrationService.deleteRegistration(registrationId);
        return Result.success();
    }

    @Operation(summary = "分页查询报名列表")
    @GetMapping("/page")
    public Result<PageResult<RegistrationListResponse>> getRegistrationPage(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小", example = "20") @RequestParam(defaultValue = "20") Integer size,
            @Parameter(description = "活动标题") @RequestParam(required = false) String activityTitle,
            @Parameter(description = "用户姓名") @RequestParam(required = false) String userName,
            @Parameter(description = "报名状态") @RequestParam(required = false) Integer status) {
        Page<RegistrationListResponse> registrationPage = registrationService.getRegistrationPage(page, size, activityTitle, userName, status);
        PageResult<RegistrationListResponse> pageResult = PageResult.success(registrationPage);
        return Result.success(pageResult);
    }

    @Operation(summary = "获取用户报名记录")
    @GetMapping("/user/{userId}")
    public Result<List<RegistrationListResponse>> getUserRegistrations(
            @Parameter(description = "用户ID", required = true) @PathVariable Long userId) {
        List<RegistrationListResponse> registrations = registrationService.getUserRegistrations(userId);
        return Result.success(registrations);
    }

    @Operation(summary = "获取活动报名记录")
    @GetMapping("/activity/{activityId}")
    public Result<List<RegistrationListResponse>> getActivityRegistrations(
            @Parameter(description = "活动ID", required = true) @PathVariable Long activityId) {
        List<RegistrationListResponse> registrations = registrationService.getActivityRegistrations(activityId);
        return Result.success(registrations);
    }

    @Operation(summary = "获取报名统计信息")
    @GetMapping("/statistics")
    public Result<RegistrationStatisticsResponse> getRegistrationStatistics() {
        RegistrationStatisticsResponse statistics = registrationService.getRegistrationStatistics();
        return Result.success(statistics);
    }
    
    @Operation(summary = "测试数据库连接")
    @GetMapping("/test-connection")
    public Result<String> testConnection() {
        try {
            Integer result = registrationMapper.testConnection();
            return Result.success("数据库连接测试成功，返回值: " + result);
        } catch (Exception e) {
            return Result.error("数据库连接测试失败: " + e.getMessage());
        }
    }
    
    @Operation(summary = "检查表结构")
    @GetMapping("/check-table")
    public Result<List<Map<String, Object>>> checkTableStructure() {
        try {
            List<Map<String, Object>> result = registrationMapper.checkTableStructure();
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("检查表结构失败: " + e.getMessage());
        }
    }
    
    @Operation(summary = "获取活动预约日历数据")
    @GetMapping("/calendar")
    public Result<List<RegistrationDetailResponse>> getCalendarRegistrations(
            @Parameter(description = "月份，格式：YYYY-MM") @RequestParam(required = false) String month,
            @Parameter(description = "活动ID") @RequestParam(required = false) Long activityId,
            @Parameter(description = "报名状态") @RequestParam(required = false) Integer status) {
        List<RegistrationDetailResponse> registrations = registrationService.getCalendarRegistrations(month, activityId, status);
        return Result.success(registrations);
    }

    @Operation(summary = "审核报名")
    @PutMapping("/{registrationId}/approve")
    public Result<Void> approveRegistration(
            @Parameter(description = "报名ID", required = true) @PathVariable Long registrationId,
            @Parameter(description = "状态", required = true) @RequestParam Integer status,
            @Parameter(description = "备注") @RequestParam(required = false) String remark) {
        try {
            registrationService.approveRegistration(registrationId, status, remark);
            return Result.success();
        } catch (BusinessException e) {
            return Result.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR.getCode(), "审核报名失败: " + e.getMessage());
        }
    }

    @Operation(summary = "取消报名")
    @PutMapping("/{registrationId}/cancel")
    public Result<Void> cancelRegistration(
            @Parameter(description = "报名ID", required = true) @PathVariable Long registrationId) {
        registrationService.cancelRegistration(registrationId);
        return Result.success();
    }
    
    @Operation(summary = "批量审核报名")
    @PostMapping("/batch-approve")
    public Result<Void> batchApproveRegistrations(
            @Parameter(description = "批量审核请求", required = true) @Valid @RequestBody BatchApproveRegistrationRequest request) {
        try {
            registrationService.batchApproveRegistrations(request.getRegistrationIds(), request.getStatus(), request.getRemark());
            return Result.success();
        } catch (BusinessException e) {
            return Result.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR.getCode(), "批量审核报名失败: " + e.getMessage());
        }
    }
}