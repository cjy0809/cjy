package com.community.center.controller;

import com.community.center.common.Result;
import com.community.center.dto.response.AnalyticsDashboardResponse;
import com.community.center.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * 数据分析控制器
 */
@Tag(name = "数据分析")
@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private static final Logger logger = LoggerFactory.getLogger(AnalyticsController.class);

    @Autowired
    private AnalyticsService analyticsService;

    @Operation(summary = "获取看板数据")
    @GetMapping("/dashboard")
    public Result<AnalyticsDashboardResponse> getDashboard(
            @Parameter(description = "开始日期") @RequestParam(required = false) 
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) 
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        logger.info("收到数据分析看板请求 - startDate: {}, endDate: {}", startDate, endDate);
        AnalyticsDashboardResponse response = analyticsService.getDashboardData(startDate, endDate);
        logger.info("数据分析看板数据获取完成 - userTrend: {}, activityParticipation: {}, serviceBookingStats: {}", 
                response.getUserTrend() != null ? response.getUserTrend().size() : 0,
                response.getActivityParticipation() != null ? response.getActivityParticipation().size() : 0,
                response.getServiceBookingStats() != null ? response.getServiceBookingStats().size() : 0);
        return Result.success(response);
    }
}
