package com.community.center.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class AnalyticsDashboardResponse {
    private List<UserTrendData> userTrend;
    private List<ActivityParticipationData> activityParticipation;
    private List<ServiceBookingStatsData> serviceBookingStats;
    private List<AgeDistributionData> ageDistribution;
    private List<RechargeDistributionData> rechargeDistribution;
    private List<ActivityTypePreferenceData> activityTypePreference;

    @Data
    public static class UserTrendData {
        private String date;
        private Long newUsers;
        private Long activeUsers;
    }

    @Data
    public static class ActivityParticipationData {
        private String date;
        private Long registrations;
        private Long participants;
    }

    @Data
    public static class ServiceBookingStatsData {
        private String date;
        private Long bookings;
        private Long completed;
        private Long cancelled;
    }

    @Data
    public static class AgeDistributionData {
        private String ageRange;
        private Long count;
    }

    @Data
    public static class RechargeDistributionData {
        private String amountRange;
        private Long count;
    }

    @Data
    public static class ActivityTypePreferenceData {
        private String type;
        private Long registrations;
        private Long participants;
    }
}
