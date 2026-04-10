package com.community.center.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.community.center.dto.response.AnalyticsDashboardResponse;
import com.community.center.entity.Activity;
import com.community.center.entity.Recharge;
import com.community.center.entity.Registration;
import com.community.center.entity.ServiceReservation;
import com.community.center.entity.User;
import com.community.center.mapper.ActivityMapper;
import com.community.center.mapper.RechargeMapper;
import com.community.center.mapper.RegistrationMapper;
import com.community.center.mapper.ServiceReservationMapper;
import com.community.center.mapper.UserMapper;
import com.community.center.service.AnalyticsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AnalyticsServiceImpl implements AnalyticsService {

    private static final Logger logger = LoggerFactory.getLogger(AnalyticsServiceImpl.class);

    @Resource
    private UserMapper userMapper;

    @Resource
    private ActivityMapper activityMapper;

    @Resource
    private RegistrationMapper registrationMapper;

    @Resource
    private ServiceReservationMapper serviceReservationMapper;

    @Resource
    private RechargeMapper rechargeMapper;

    @Override
    public AnalyticsDashboardResponse getDashboardData(LocalDate startDate, LocalDate endDate) {
        logger.info("开始获取数据分析看板数据 - startDate: {}, endDate: {}", startDate, endDate);
        
        AnalyticsDashboardResponse response = new AnalyticsDashboardResponse();

        if (startDate == null) {
            startDate = LocalDate.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        logger.info("查询时间范围: {} 至 {}", startDateTime, endDateTime);

        response.setUserTrend(getUserTrend(startDateTime, endDateTime));
        response.setActivityParticipation(getActivityParticipation(startDateTime, endDateTime));
        response.setServiceBookingStats(getServiceBookingStats(startDateTime, endDateTime));
        response.setAgeDistribution(getAgeDistribution());
        response.setRechargeDistribution(getRechargeDistribution(startDateTime, endDateTime));
        response.setActivityTypePreference(getActivityTypePreference(startDateTime, endDateTime));

        logger.info("数据分析看板数据获取完成");
        return response;
    }

    private List<AnalyticsDashboardResponse.UserTrendData> getUserTrend(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        List<AnalyticsDashboardResponse.UserTrendData> result = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(startDateTime.toLocalDate(), endDateTime.toLocalDate());
        int days = (int) Math.min(daysBetween, 30);

        for (int i = 0; i <= days; i++) {
            LocalDateTime dayStart = startDateTime.plusDays(i).toLocalDate().atStartOfDay();
            LocalDateTime dayEnd = startDateTime.plusDays(i).toLocalDate().atTime(23, 59, 59);

            LambdaQueryWrapper<User> newUsersWrapper = new LambdaQueryWrapper<>();
            newUsersWrapper.between(User::getCreateTime, dayStart, dayEnd);
            Long newUsers = userMapper.selectCount(newUsersWrapper);

            LambdaQueryWrapper<Registration> activeUsersWrapper = new LambdaQueryWrapper<>();
            activeUsersWrapper.between(Registration::getCreateTime, dayStart, dayEnd);
            Long activeUsers = registrationMapper.selectCount(activeUsersWrapper);

            AnalyticsDashboardResponse.UserTrendData data = new AnalyticsDashboardResponse.UserTrendData();
            data.setDate(dayStart.toLocalDate().format(formatter));
            data.setNewUsers(newUsers);
            data.setActiveUsers(activeUsers);
            result.add(data);
        }

        return result;
    }

    private List<AnalyticsDashboardResponse.ActivityParticipationData> getActivityParticipation(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        List<AnalyticsDashboardResponse.ActivityParticipationData> result = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(startDateTime.toLocalDate(), endDateTime.toLocalDate());
        int days = (int) Math.min(daysBetween, 30);

        for (int i = 0; i <= days; i++) {
            LocalDateTime dayStart = startDateTime.plusDays(i).toLocalDate().atStartOfDay();
            LocalDateTime dayEnd = startDateTime.plusDays(i).toLocalDate().atTime(23, 59, 59);

            LambdaQueryWrapper<Registration> registrationWrapper = new LambdaQueryWrapper<>();
            registrationWrapper.between(Registration::getCreateTime, dayStart, dayEnd);
            Long registrations = registrationMapper.selectCount(registrationWrapper);

            LambdaQueryWrapper<Registration> participantWrapper = new LambdaQueryWrapper<>();
            participantWrapper.between(Registration::getCreateTime, dayStart, dayEnd);
            participantWrapper.eq(Registration::getStatus, 2);
            Long participants = registrationMapper.selectCount(participantWrapper);

            AnalyticsDashboardResponse.ActivityParticipationData data = new AnalyticsDashboardResponse.ActivityParticipationData();
            data.setDate(dayStart.toLocalDate().format(formatter));
            data.setRegistrations(registrations);
            data.setParticipants(participants);
            result.add(data);
        }

        return result;
    }

    private List<AnalyticsDashboardResponse.ServiceBookingStatsData> getServiceBookingStats(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        List<AnalyticsDashboardResponse.ServiceBookingStatsData> result = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(startDateTime.toLocalDate(), endDateTime.toLocalDate());
        int days = (int) Math.min(daysBetween, 30);

        for (int i = 0; i <= days; i++) {
            LocalDateTime dayStart = startDateTime.plusDays(i).toLocalDate().atStartOfDay();
            LocalDateTime dayEnd = startDateTime.plusDays(i).toLocalDate().atTime(23, 59, 59);

            LambdaQueryWrapper<ServiceReservation> bookingWrapper = new LambdaQueryWrapper<>();
            bookingWrapper.between(ServiceReservation::getCreateTime, dayStart, dayEnd);
            Long bookings = serviceReservationMapper.selectCount(bookingWrapper);

            LambdaQueryWrapper<ServiceReservation> completedWrapper = new LambdaQueryWrapper<>();
            completedWrapper.between(ServiceReservation::getCreateTime, dayStart, dayEnd);
            completedWrapper.eq(ServiceReservation::getStatus, 2);
            Long completed = serviceReservationMapper.selectCount(completedWrapper);

            LambdaQueryWrapper<ServiceReservation> cancelledWrapper = new LambdaQueryWrapper<>();
            cancelledWrapper.between(ServiceReservation::getCreateTime, dayStart, dayEnd);
            cancelledWrapper.eq(ServiceReservation::getStatus, 5);
            Long cancelled = serviceReservationMapper.selectCount(cancelledWrapper);

            AnalyticsDashboardResponse.ServiceBookingStatsData data = new AnalyticsDashboardResponse.ServiceBookingStatsData();
            data.setDate(dayStart.toLocalDate().format(formatter));
            data.setBookings(bookings);
            data.setCompleted(completed);
            data.setCancelled(cancelled);
            result.add(data);
        }

        return result;
    }

    private List<AnalyticsDashboardResponse.AgeDistributionData> getAgeDistribution() {
        List<User> users = userMapper.selectList(null);

        Map<String, Long> ageDistribution = new HashMap<>();
        ageDistribution.put("60-65岁", 0L);
        ageDistribution.put("66-70岁", 0L);
        ageDistribution.put("71-75岁", 0L);
        ageDistribution.put("76-80岁", 0L);
        ageDistribution.put("81-85岁", 0L);
        ageDistribution.put("86岁以上", 0L);

        for (User user : users) {
            if (user.getAge() == null) continue;

            int age = user.getAge();
            if (age >= 60 && age <= 65) {
                ageDistribution.put("60-65岁", ageDistribution.get("60-65岁") + 1);
            } else if (age >= 66 && age <= 70) {
                ageDistribution.put("66-70岁", ageDistribution.get("66-70岁") + 1);
            } else if (age >= 71 && age <= 75) {
                ageDistribution.put("71-75岁", ageDistribution.get("71-75岁") + 1);
            } else if (age >= 76 && age <= 80) {
                ageDistribution.put("76-80岁", ageDistribution.get("76-80岁") + 1);
            } else if (age >= 81 && age <= 85) {
                ageDistribution.put("81-85岁", ageDistribution.get("81-85岁") + 1);
            } else if (age >= 86) {
                ageDistribution.put("86岁以上", ageDistribution.get("86岁以上") + 1);
            }
        }

        List<AnalyticsDashboardResponse.AgeDistributionData> result = new ArrayList<>();
        ageDistribution.forEach((ageRange, count) -> {
            AnalyticsDashboardResponse.AgeDistributionData data = new AnalyticsDashboardResponse.AgeDistributionData();
            data.setAgeRange(ageRange);
            data.setCount(count);
            result.add(data);
        });

        return result;
    }

    private List<AnalyticsDashboardResponse.RechargeDistributionData> getRechargeDistribution(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        LambdaQueryWrapper<Recharge> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Recharge::getStatus, 1);
        wrapper.between(Recharge::getCreateTime, startDateTime, endDateTime);
        List<Recharge> recharges = rechargeMapper.selectList(wrapper);

        Map<String, Long> distribution = new HashMap<>();
        distribution.put("0-100元", 0L);
        distribution.put("101-500元", 0L);
        distribution.put("501-1000元", 0L);
        distribution.put("1000元以上", 0L);

        for (Recharge recharge : recharges) {
            BigDecimal amount = recharge.getAmount();
            if (amount == null) continue;

            double value = amount.doubleValue();
            if (value <= 100) {
                distribution.put("0-100元", distribution.get("0-100元") + 1);
            } else if (value > 100 && value <= 500) {
                distribution.put("101-500元", distribution.get("101-500元") + 1);
            } else if (value > 500 && value <= 1000) {
                distribution.put("501-1000元", distribution.get("501-1000元") + 1);
            } else {
                distribution.put("1000元以上", distribution.get("1000元以上") + 1);
            }
        }

        List<AnalyticsDashboardResponse.RechargeDistributionData> result = new ArrayList<>();
        result.add(createRechargeDistributionData("0-100元", distribution.get("0-100元")));
        result.add(createRechargeDistributionData("101-500元", distribution.get("101-500元")));
        result.add(createRechargeDistributionData("501-1000元", distribution.get("501-1000元")));
        result.add(createRechargeDistributionData("1000元以上", distribution.get("1000元以上")));

        return result;
    }

    private List<AnalyticsDashboardResponse.ActivityTypePreferenceData> getActivityTypePreference(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        LambdaQueryWrapper<Registration> wrapper = new LambdaQueryWrapper<>();
        wrapper.between(Registration::getCreateTime, startDateTime, endDateTime);
        List<Registration> registrations = registrationMapper.selectList(wrapper);

        Map<Long, Activity> activityMap = new HashMap<>();
        if (!registrations.isEmpty()) {
            List<Long> activityIds = registrations.stream()
                    .map(Registration::getActivityId)
                    .distinct()
                    .collect(Collectors.toList());
            
            if (!activityIds.isEmpty()) {
                LambdaQueryWrapper<Activity> activityWrapper = new LambdaQueryWrapper<>();
                activityWrapper.in(Activity::getId, activityIds);
                List<Activity> activities = activityMapper.selectList(activityWrapper);
                activityMap = activities.stream()
                        .collect(Collectors.toMap(Activity::getId, a -> a));
            }
        }

        Map<String, Long> typeStats = new HashMap<>();
        for (Registration registration : registrations) {
            Activity activity = activityMap.get(registration.getActivityId());
            String type = "其他";
            if (activity != null && activity.getTitle() != null) {
                type = activity.getTitle();
            }
            typeStats.put(type, typeStats.getOrDefault(type, 0L) + 1);
        }

        List<AnalyticsDashboardResponse.ActivityTypePreferenceData> result = new ArrayList<>();
        typeStats.forEach((type, count) -> {
            AnalyticsDashboardResponse.ActivityTypePreferenceData data = new AnalyticsDashboardResponse.ActivityTypePreferenceData();
            data.setType(type);
            data.setRegistrations(count);
            data.setParticipants(count);
            result.add(data);
        });

        return result;
    }

    private AnalyticsDashboardResponse.RechargeDistributionData createRechargeDistributionData(String amountRange, Long count) {
        AnalyticsDashboardResponse.RechargeDistributionData data = new AnalyticsDashboardResponse.RechargeDistributionData();
        data.setAmountRange(amountRange);
        data.setCount(count);
        return data;
    }
}
