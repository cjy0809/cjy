package com.community.center.service;

import com.community.center.dto.response.AnalyticsDashboardResponse;

import java.time.LocalDate;

/**
 * 数据分析服务
 */
public interface AnalyticsService {

    AnalyticsDashboardResponse getDashboardData(LocalDate startDate, LocalDate endDate);
}
