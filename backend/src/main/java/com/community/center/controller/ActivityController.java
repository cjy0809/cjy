package com.community.center.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.center.common.PageResult;
import com.community.center.common.Result;
import com.community.center.common.ResultCode;
import com.community.center.dto.request.ActivityCreateRequest;
import com.community.center.dto.request.ActivityUpdateRequest;
import com.community.center.dto.response.ActivityDetailResponse;
import com.community.center.exception.BusinessException;
import com.community.center.service.ActivityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 活动控制器
 * 处理社区中心活动的创建、查询、报名、取消报名等操作
 */
@Tag(name = "活动管理")
@RestController
@RequestMapping("/api/activity")
public class ActivityController {

    @Autowired
    private ActivityService activityService;

    @Operation(summary = "创建活动")
    @PostMapping
    public Result<Void> createActivity(@Valid @RequestBody ActivityCreateRequest request) {
        activityService.createActivity(request);
        return Result.success();
    }

    @Operation(summary = "更新活动")
    @PutMapping("/{activityId}")
    public Result<Void> updateActivity(
            @Parameter(description = "活动ID", required = true) @PathVariable Long activityId,
            @Valid @RequestBody ActivityUpdateRequest request) {
        activityService.updateActivity(activityId, request);
        return Result.success();
    }

    @Operation(summary = "删除活动")
    @DeleteMapping("/{activityId}")
    public Result<Void> deleteActivity(
            @Parameter(description = "活动ID", required = true) @PathVariable Long activityId) {
        activityService.deleteActivity(activityId);
        return Result.success();
    }

    @Operation(summary = "分页查询活动")
    @GetMapping("/page")
    public Result<PageResult<ActivityDetailResponse>> getActivityPage(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小", example = "20") @RequestParam(defaultValue = "20") Integer size,
            @Parameter(description = "活动标题") @RequestParam(required = false) String title,
            @Parameter(description = "活动状态") @RequestParam(required = false, name = "status") String status) {
        Page<ActivityDetailResponse> activityPage = activityService.getActivityPage(page, size, title, status);
        PageResult<ActivityDetailResponse> pageResult = PageResult.success(activityPage);
        return Result.success(pageResult);
    }

    @Operation(summary = "报名活动")
    @PostMapping("/{activityId}/register")
    public Result<Void> registerActivity(
            @Parameter(description = "活动ID", required = true) @PathVariable Long activityId) {
        activityService.registerActivity(activityId);
        return Result.success();
    }

    @Operation(summary = "取消报名")
    @DeleteMapping("/{activityId}/register")
    public Result<Void> cancelRegistration(
            @Parameter(description = "活动ID", required = true) @PathVariable Long activityId) {
        activityService.cancelRegistration(activityId);
        return Result.success();
    }

    @Operation(summary = "获取活动详情")
    @GetMapping("/{activityId}")
    public Result<ActivityDetailResponse> getActivityDetail(
            @Parameter(description = "活动ID", required = true) @PathVariable Long activityId) {
        ActivityDetailResponse activityDetail = activityService.getActivityDetail(activityId);
        return Result.success(activityDetail);
    }

    @Operation(summary = "获取相关活动")
    @GetMapping("/{activityId}/related")
    public Result<List<ActivityDetailResponse>> getRelatedActivities(
            @Parameter(description = "活动ID", required = true) @PathVariable Long activityId,
            @Parameter(description = "限制数量", example = "5") @RequestParam(defaultValue = "5") Integer limit) {
        List<ActivityDetailResponse> relatedActivities = activityService.getRelatedActivities(activityId, limit);
        return Result.success(relatedActivities);
    }

    @Operation(summary = "更新活动状态")
    @PutMapping("/{activityId}/status")
    public Result<Void> updateActivityStatus(
            @Parameter(description = "活动ID", required = true) @PathVariable Long activityId,
            @Parameter(description = "状态", required = true) @RequestParam Integer status) {
        try {
            activityService.updateActivityStatus(activityId, status);
            return Result.success();
        } catch (BusinessException e) {
            return Result.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR.getCode(), "更新活动状态失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取活动统计数据")
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getActivityStatistics() {
        Map<String, Object> statistics = activityService.getActivityStatistics();
        return Result.success(statistics);
    }

    @Operation(summary = "获取可报名的活动列表")
    @GetMapping("/registrable")
    public Result<List<ActivityDetailResponse>> getRegistrableActivities() {
        List<ActivityDetailResponse> activities = activityService.getRegistrableActivities();
        return Result.success(activities);
    }
}
