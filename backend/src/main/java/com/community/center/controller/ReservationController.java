package com.community.center.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.center.common.PageResult;
import com.community.center.common.Result;
import com.community.center.dto.request.ReservationCreateRequest;
import com.community.center.dto.response.ReservationResponse;
import com.community.center.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 预约控制器
 * 处理社区中心场地预约的创建、查询、审核、取消等操作
 */
@Tag(name = "预约管理")
@RestController
@RequestMapping("/api/reservation")
public class ReservationController {

    @Autowired
    private ReservationService reservationService;

    @Operation(summary = "获取日历视图预约数据")
    @GetMapping("/calendar")
    public Result<List<ReservationResponse>> getCalendarReservations(
            @Parameter(description = "月份，格式：YYYY-MM") @RequestParam String month,
            @Parameter(description = "场地ID") @RequestParam(required = false) Long venueId,
            @Parameter(description = "预约状态") @RequestParam(required = false) Integer status) {
        List<ReservationResponse> reservations = reservationService.getCalendarReservations(month, venueId, status);
        return Result.success(reservations);
    }
    
    @Operation(summary = "获取预约详情")
    @GetMapping("/{reservationId}")
    public Result<ReservationResponse> getReservationDetail(
            @Parameter(description = "预约ID", required = true) @PathVariable Long reservationId) {
        ReservationResponse response = reservationService.getReservationDetail(reservationId);
        return Result.success(response);
    }

    @Operation(summary = "分页查询预约")
    @GetMapping("/page")
    public Result<PageResult<ReservationResponse>> getReservationPage(
            @Parameter(description = "页码", required = true) @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页大小", required = true) @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "用户ID") @RequestParam(required = false) Long userId,
            @Parameter(description = "场地ID") @RequestParam(required = false) Long venueId,
            @Parameter(description = "场地名称") @RequestParam(required = false) String venueName,
            @Parameter(description = "预约人姓名") @RequestParam(required = false) String userName,
            @Parameter(description = "预约状态") @RequestParam(required = false) Integer status) {
        
        // 添加调试日志
        System.out.println("接收到的搜索参数 - venueName: " + venueName + ", userName: " + userName + ", status: " + status);
        
        // 处理空字符串参数 - 只处理null，保留非空字符串
        String processedVenueName = venueName;
        String processedUserName = userName;
        
        Page<ReservationResponse> reservationPage = reservationService.getReservationPage(pageNum, pageSize, userId, venueId, processedVenueName, processedUserName, status, null, null);
        PageResult<ReservationResponse> pageResult = PageResult.success(reservationPage);
        return Result.success(pageResult);
    }
    
    @Operation(summary = "创建预约")
    @PostMapping
    public Result<Boolean> createReservation(@RequestBody @NonNull ReservationCreateRequest request) {
        Boolean success = reservationService.createReservation(request);
        return Result.success(success);
    }
    
    @Operation(summary = "更新预约")
    @PutMapping("/{id}")
    public Result<Boolean> updateReservation(
            @Parameter(description = "预约ID", required = true) @PathVariable Long id,
            @RequestBody @NonNull ReservationCreateRequest request) {
        Boolean success = reservationService.updateReservation(id, request);
        return Result.success(success);
    }
    
    @Operation(summary = "删除预约")
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteReservation(
            @Parameter(description = "预约ID", required = true) @PathVariable Long id) {
        Boolean success = reservationService.deleteReservation(id);
        return Result.success(success);
    }

    @Operation(summary = "取消预约")
    @PutMapping("/{reservationId}/cancel")
    public Result<Void> cancelReservation(
            @Parameter(description = "预约ID", required = true) @PathVariable Long reservationId) {
        reservationService.cancelReservation(reservationId);
        return Result.success();
    }
    
    @Operation(summary = "审核预约")
    @PutMapping("/{id}/review")
    public Result<Boolean> reviewReservation(
            @Parameter(description = "预约ID", required = true) @PathVariable Long id,
            @Parameter(description = "审核状态", required = true) @RequestParam Integer status,
            @Parameter(description = "审核意见") @RequestParam(required = false) String comment) {
        // 暂时使用固定审核人ID，实际应从登录信息中获取
        Boolean success = reservationService.reviewReservation(id, status, comment, 1L);
        return Result.success(success);
    }

    @Operation(summary = "批量取消过期预约")
    @PostMapping("/cancel-expired")
    public Result<Void> cancelExpiredReservations() {
        reservationService.cancelExpiredReservations();
        return Result.success();
    }
    
    @Operation(summary = "获取场地预约人数统计")
    @GetMapping("/venue/{venueId}/statistics")
    public Result<Map<String, Object>> getVenueReservationStatistics(
            @Parameter(description = "场地ID", required = true) @PathVariable Long venueId,
            @Parameter(description = "开始日期") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        Map<String, Object> statistics = reservationService.getVenueReservationStatistics(venueId, startDate, endDate);
        return Result.success(statistics);
    }
}
