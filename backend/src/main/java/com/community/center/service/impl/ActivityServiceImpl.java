package com.community.center.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.community.center.common.ResultCode;
import com.community.center.dto.request.ActivityCreateRequest;
import com.community.center.dto.request.ActivityUpdateRequest;
import com.community.center.dto.response.ActivityDetailResponse;
import com.community.center.entity.Activity;
import com.community.center.entity.Registration;
import com.community.center.entity.User;
import com.community.center.entity.Venue;
import com.community.center.exception.BusinessException;
import com.community.center.mapper.ActivityMapper;
import com.community.center.mapper.RegistrationMapper;
import com.community.center.mapper.VenueMapper;
import com.community.center.service.ActivityService;
import com.community.center.service.UserService;
import com.community.center.service.RegistrationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 活动服务实现类
 */
@Slf4j
@Service
public class ActivityServiceImpl extends ServiceImpl<ActivityMapper, Activity> implements ActivityService {

    @Resource
    private ActivityMapper activityMapper;
    
    @Resource
    private RegistrationMapper registrationMapper;
    
    @Resource
    private VenueMapper venueMapper;
    
    @Resource
    private UserService userService;
    
    @Resource
    @Lazy
    private RegistrationService registrationService;

    @Override
    public void createActivity(ActivityCreateRequest request) {
        // 检查时间是否合理
        if (request.getStartTime().isAfter(request.getEndTime())) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "开始时间不能晚于结束时间");
        }
        
        if (request.getVenueId() != null) {
            Venue venue = venueMapper.selectById(request.getVenueId());
            if (venue != null) {
                request.setLocation(venue.getName());
                request.setMaxParticipants(venue.getCapacity());
            }
        }
        
        // 创建活动
        Activity activity = new Activity();
        BeanUtils.copyProperties(request, activity);
        
        Long organizerId = getCurrentUserId();
        activity.setOrganizerId(organizerId);
        
        activity.setCurrentParticipants(0);
        activity.setStatus(0); 
        
        this.save(activity);
    }

    @Override
    public void updateActivity(Long activityId, ActivityUpdateRequest request) {
        Activity activity = this.getById(activityId);
        if (activity == null) {
            throw new BusinessException(ResultCode.ACTIVITY_NOT_FOUND);
        }

        LocalDateTime startTime = request.getStartTime() != null ? request.getStartTime() : activity.getStartTime();
        LocalDateTime endTime = request.getEndTime() != null ? request.getEndTime() : activity.getEndTime();

        if (startTime != null && endTime != null && startTime.isAfter(endTime)) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "开始时间不能晚于结束时间");
        }

        // 更新活动信息
        if (StringUtils.hasText(request.getTitle())) {
            activity.setTitle(request.getTitle());
        }
        if (StringUtils.hasText(request.getContent())) {
            activity.setContent(request.getContent());
        }
        if (StringUtils.hasText(request.getCoverImage())) {
            activity.setCoverImage(request.getCoverImage());
        }
        if (request.getOrganizerId() != null) {
            activity.setOrganizerId(request.getOrganizerId());
        }
        if (StringUtils.hasText(request.getLocation())) {
            activity.setLocation(request.getLocation());
        }
        if (request.getMaxParticipants() != null) {
            activity.setMaxParticipants(request.getMaxParticipants());
        }
        if (request.getStartTime() != null) {
            activity.setStartTime(request.getStartTime());
        }
        if (request.getEndTime() != null) {
            activity.setEndTime(request.getEndTime());
        }
        if (request.getStatus() != null) {
            activity.setStatus(request.getStatus());
        }

        // 如果更新了时间且活动未被取消，自动计算状态
        if ((request.getStartTime() != null || request.getEndTime() != null) && activity.getStatus() != 3) {
            LocalDateTime now = LocalDateTime.now();
            int newStatus;
            if (now.isBefore(activity.getStartTime())) {
                newStatus = 0; // 未开始
            } else if (!now.isAfter(activity.getStartTime()) && !now.isAfter(activity.getEndTime())) {
                newStatus = 1; // 进行中
            } else {
                newStatus = 2; // 已结束
            }
            activity.setStatus(newStatus);
        }

        this.updateById(activity);
    }

    @Override
    public void deleteActivity(Long activityId) {
        Activity activity = this.getById(activityId);
        if (activity == null) {
            throw new BusinessException(ResultCode.ACTIVITY_NOT_FOUND);
        }
        this.removeById(activityId);
    }

    @Override
    public Page<ActivityDetailResponse> getActivityPage(Integer page, Integer size, String title, String status) {
        try {
            Integer statusInt = null;
            if (status != null && !status.isEmpty()) {
                try {
                    statusInt = Integer.valueOf(status);
                } catch (NumberFormatException e) {
                    statusInt = null;
                }
            }
            
            Page<Activity> activityPage = activityMapper.selectActivityPage(new Page<>(page, size), title, statusInt);
            
            Page<ActivityDetailResponse> responsePage = new Page<>(page, size);
            
            List<ActivityDetailResponse> responseList = activityPage.getRecords().stream()
                .map((@NonNull Activity activity) -> {
                    ActivityDetailResponse detail = new ActivityDetailResponse();
                    BeanUtils.copyProperties(activity, detail);
                    detail.setIsRegistered(false);
                    return detail;
                })
                .collect(Collectors.toList());
            
            responsePage.setTotal(activityPage.getTotal());
            responsePage.setRecords(responseList);
            return responsePage;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("获取活动列表失败：" + e.getMessage());
        }
    }

    @Override
    public ActivityDetailResponse getActivityDetail(Long activityId) {
        Activity activity = this.getById(activityId);
        if (activity == null) {
            throw new BusinessException(ResultCode.ACTIVITY_NOT_FOUND);
        }
        
        ActivityDetailResponse detail = new ActivityDetailResponse();
        BeanUtils.copyProperties(activity, detail);
        
        if (activity.getOrganizerId() != null) {
            try {
                User organizer = userService.getById(activity.getOrganizerId());
                if (organizer != null) {
                    detail.setOrganizerName(organizer.getName());
                    detail.setOrganizerTitle("社区管理员");
                    detail.setOrganizerPhone(organizer.getPhone());
                    detail.setOrganizerAvatar(organizer.getAvatar());
                }
            } catch (Exception e) {
            }
        }
        
        Long userId = null;
        try {
            userId = getCurrentUserId();
        } catch (Exception e) {
        }
        
        if (userId != null) {
            LambdaQueryWrapper<Registration> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Registration::getUserId, userId)
                       .eq(Registration::getActivityId, activityId);
            Registration registration = registrationMapper.selectOne(queryWrapper);
            detail.setIsRegistered(registration != null);
        } else {
            detail.setIsRegistered(false);
        }
        
        return detail;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void registerActivity(Long activityId) {
        Long userId = getCurrentUserId();
        
        Activity activity = this.getById(activityId);
        if (activity == null) {
            throw new BusinessException(ResultCode.ACTIVITY_NOT_FOUND);
        }
        
        if (activity.getStatus() == 3) {
            throw new BusinessException(ResultCode.ACTIVITY_NOT_REGISTRABLE);
        }
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = activity.getStartTime();
        LocalDateTime endTime = activity.getEndTime();
        
        if (startTime == null || endTime == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "活动时间未设置");
        }
        
        LocalDateTime oneWeekBeforeStart = startTime.minusWeeks(1);// 计算活动开始时间前一周
        LocalDateTime oneHourBeforeEnd = endTime.minusHours(1);// 计算活动结束时间前一小时
        
        if (now.isBefore(oneWeekBeforeStart)) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "未到报名时间，请在活动开始前一周内报名");
        }
        
        if (now.isAfter(oneHourBeforeEnd) || now.isEqual(oneHourBeforeEnd)) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "活动即将结束，无法报名");
        }
        
        // 检查是否已报名
        LambdaQueryWrapper<Registration> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Registration::getUserId, userId)
                   .eq(Registration::getActivityId, activityId);
        Registration existRegistration = registrationMapper.selectOne(queryWrapper);
        if (existRegistration != null) {
            throw new BusinessException(ResultCode.ALREADY_REGISTERED);
        }
        
        if (activity.getCurrentParticipants() >= activity.getMaxParticipants()) {
            throw new BusinessException(ResultCode.ACTIVITY_FULL);
        }// 检查人数是否已满
        
        
        Registration registration = new Registration();
        registration.setUserId(userId);
        registration.setActivityId(activityId);
        registration.setRegistrationTime(LocalDateTime.now());
        registration.setStatus(1);
        registration.setRegistrationType(0);
        registrationMapper.insert(registration);// 创建报名记录
        
        int updateResult = baseMapper.increaseCurrentParticipants(activityId);
        if (updateResult != 1) {
            throw new RuntimeException("增加活动参与人数失败");
        }// 增加当前参与人数
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelRegistration(Long activityId) {
        Long userId = getCurrentUserId();
    
        LambdaQueryWrapper<Registration> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Registration::getUserId, userId)
                   .eq(Registration::getActivityId, activityId);
        Registration registration = registrationMapper.selectOne(queryWrapper);
        if (registration == null) {
            throw new BusinessException(ResultCode.NOT_REGISTERED);
        }
        
        registrationMapper.deleteById(registration.getId());
        int updateResult = baseMapper.decreaseCurrentParticipants(activityId);
        if (updateResult != 1) {
            throw new RuntimeException("减少活动参与人数失败");
        }
    }

    @Override
    public void updateActivityStatus(Long activityId, Integer status) {
        Activity activity = this.getById(activityId);
        if (activity == null) {
            throw new BusinessException(ResultCode.ACTIVITY_NOT_FOUND);
        }
        if (status == null || status < 0 || status > 3) {
            throw new BusinessException(ResultCode.PARAM_VALIDATION_ERROR.getCode(), "无效的活动状态值，允许的值: 0(未开始), 1(进行中), 2(已结束), 3(已取消)");
        }
        
        activity.setStatus(status);
        this.updateById(activity);
    }
    
    @Override
    public List<ActivityDetailResponse> getRelatedActivities(Long activityId, Integer limit) {
        Activity currentActivity = this.getById(activityId);
        if (currentActivity == null) {
            return new ArrayList<>();
        }
        
        LambdaQueryWrapper<Activity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.ne(Activity::getId, activityId) // 排除当前活动
                   .eq(Activity::getStatus, 1) // 只查询进行中的活动
                   .gt(Activity::getEndTime, LocalDateTime.now()); // 只查询未结束的活动
        
        List<Activity> allActivities = this.list(queryWrapper);
        
        if (allActivities.isEmpty()) {
            return new ArrayList<>();
        }
        
        Map<Activity, Double> activityScores = new HashMap<>();
        Random random = new Random();
        
        for (Activity activity : allActivities) {
            double score = 0.0;
            
            if (currentActivity.getLocation() != null && activity.getLocation() != null) {
                if (currentActivity.getLocation().equals(activity.getLocation())) {
                    score += 0.3;
                } else {
                    String[] currentLocationWords = currentActivity.getLocation().split("[\\s,，]+");
                    String[] activityLocationWords = activity.getLocation().split("[\\s,，]+");
                    
                    for (String currentWord : currentLocationWords) {
                        for (String activityWord : activityLocationWords) {
                            if (currentWord.length() > 1 && activityWord.length() > 1 && 
                                (currentWord.contains(activityWord) || activityWord.contains(currentWord))) {
                                score += 0.1;
                                break;
                            }
                        }
                    }
                }
            }
            
            if (currentActivity.getStartTime() != null && activity.getStartTime() != null) {
                long daysBetween = ChronoUnit.DAYS.between(
                    currentActivity.getStartTime().toLocalDate(), 
                    activity.getStartTime().toLocalDate());
                
                if (daysBetween <= 7) { 
                    score += 0.3 * (1 - daysBetween / 7.0);
                }
            }
            
            if (currentActivity.getTitle() != null && activity.getTitle() != null) {
                String[] currentTitleWords = currentActivity.getTitle().split("[\\s,，。！？]+");
                String[] activityTitleWords = activity.getTitle().split("[\\s,，。！？]+");
                
                Set<String> currentWordSet = Set.of(currentTitleWords);
                Set<String> activityWordSet = Set.of(activityTitleWords);
                
                long commonWords = currentWordSet.stream()
                    .filter(word -> word.length() > 1 && activityWordSet.contains(word))
                    .count();
                
                if (commonWords > 0) {
                    score += 0.2 * (commonWords / (double) Math.min(currentTitleWords.length, activityTitleWords.length));
                }
            }
            
            score += random.nextDouble() * 0.2;
            
            activityScores.put(activity, score);
        }
        
        List<Activity> sortedActivities = activityScores.entrySet().stream()
            .sorted(Map.Entry.<Activity, Double>comparingByValue().reversed())
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
        
        List<Activity> limitedActivities = sortedActivities.stream()
            .limit(limit)
            .collect(Collectors.toList());
        
        List<ActivityDetailResponse> result = limitedActivities.stream().map((@NonNull Activity activity) -> {
            ActivityDetailResponse detail = new ActivityDetailResponse();
            BeanUtils.copyProperties(activity, detail);
            detail.setIsRegistered(false);
            
            if (activity.getOrganizerId() != null) {
                try {
                    User organizer = userService.getById(activity.getOrganizerId());
                    if (organizer != null) {
                        detail.setOrganizerName(organizer.getName());
                        detail.setOrganizerTitle("社区管理员");
                        detail.setOrganizerPhone(organizer.getPhone());
                        detail.setOrganizerAvatar(organizer.getAvatar());
                    }
                } catch (Exception e) {
                }
            }
            
            return detail;
        }).collect(Collectors.toList());
        
        return result;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteActivitiesByOrganizerId(Long organizerId) {
        LambdaQueryWrapper<Activity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Activity::getOrganizerId, organizerId);
        List<Activity> activities = this.list(queryWrapper);
        
        if (activities.isEmpty()) {
            return;
        }
        
        for (Activity activity : activities) {
            try {
                registrationService.deleteRegistrationsByActivityId(activity.getId());
                this.removeById(activity.getId());
            } catch (Exception e) {
                throw new BusinessException("删除活动失败: " + e.getMessage());
            }
        }
    }

    
    @Override
    public Map<String, Object> getActivityStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        
        QueryWrapper<Activity> wrapper = new QueryWrapper<>();
        List<Activity> allActivities = activityMapper.selectList(wrapper);
        
        LocalDate now = LocalDate.now();
        LocalDate lastMonthStart = now.minusMonths(1).withDayOfMonth(1);
        LocalDate lastMonthEnd = now.withDayOfMonth(1).minusDays(1);
        
        List<Activity> lastMonthActivities = allActivities.stream()
            .filter(a -> a.getCreateTime() != null)
            .filter(a -> {
                LocalDate createDate = a.getCreateTime().toLocalDate();
                return !createDate.isBefore(lastMonthStart) && !createDate.isAfter(lastMonthEnd);
            })
            .collect(Collectors.toList());
        
        LocalDateTime currentDateTime = LocalDateTime.now();
        
        statistics.put("totalActivities", (long) allActivities.size());
        statistics.put("activeActivities", allActivities.stream().filter(a -> calculateActivityStatus(a, currentDateTime) == 1).count());
        statistics.put("upcomingActivities", allActivities.stream().filter(a -> calculateActivityStatus(a, currentDateTime) == 0).count());
        statistics.put("endedActivities", allActivities.stream().filter(a -> calculateActivityStatus(a, currentDateTime) == 2).count());
        statistics.put("canceledActivities", allActivities.stream().filter(a -> calculateActivityStatus(a, currentDateTime) == 3).count());
        
        statistics.put("lastMonthTotalActivities", (long) lastMonthActivities.size());
        statistics.put("lastMonthActiveActivities", lastMonthActivities.stream().filter(a -> calculateActivityStatus(a, currentDateTime) == 1).count());
        statistics.put("lastMonthUpcomingActivities", lastMonthActivities.stream().filter(a -> calculateActivityStatus(a, currentDateTime) == 0).count());
        statistics.put("lastMonthEndedActivities", lastMonthActivities.stream().filter(a -> calculateActivityStatus(a, currentDateTime) == 2).count());
        statistics.put("lastMonthCanceledActivities", lastMonthActivities.stream().filter(a -> calculateActivityStatus(a, currentDateTime) == 3).count());
        
        return statistics;
    }
    
    private int calculateActivityStatus(Activity activity, LocalDateTime currentDateTime) {
        if (activity.getStatus() == 3) return 3;
        
        if (activity.getStartTime() == null || activity.getEndTime() == null) {
            return activity.getStatus();
        }
        
        if (currentDateTime.isBefore(activity.getStartTime())) {
            return 0;
        } else if (!currentDateTime.isAfter(activity.getStartTime()) && !currentDateTime.isAfter(activity.getEndTime())) {
            return 1;
        } else {
            return 2;
        }
    }
    
    /**
     * 获取当前用户ID
     */
    private Long getCurrentUserId() {
        try {
            // 从SecurityContext中获取当前用户ID
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null) {
                // 从请求属性中获取userId，该属性在JwtFilter中设置
                RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
                if (requestAttributes instanceof ServletRequestAttributes) {
                    HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
                    Object userIdObj = request.getAttribute("userId");
                    if (userIdObj != null) {
                        return Long.valueOf(userIdObj.toString());
                    }
                }
            }
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        } catch (Exception e) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
    }

    @Override
    public List<ActivityDetailResponse> getRegistrableActivities() {
        LocalDateTime now = LocalDateTime.now();
        
        try {
            // 查询所有活动
            List<Activity> allActivities = this.list(new LambdaQueryWrapper<>());
            
            // 过滤出可报名的活动
            List<Activity> registrableActivities = allActivities.stream()
                .filter(activity -> {
                    try {
                        // 检查活动状态 - 已取消的活动不能报名
                        if (activity.getStatus() == null || activity.getStatus() == 3) {
                            return false;
                        }
                        
                        // 检查活动时间是否设置
                        if (activity.getStartTime() == null || activity.getEndTime() == null) {
                            return false;
                        }
                        
                        // 计算活动开始时间前一周
                        LocalDateTime oneWeekBeforeStart = activity.getStartTime().minusWeeks(1);
                        
                        // 计算活动结束时间前一小时
                        LocalDateTime oneHourBeforeEnd = activity.getEndTime().minusHours(1);
                        
                        // 检查是否在报名时间窗口内（活动开始前一周到活动结束前一小时）
                        if (now.isBefore(oneWeekBeforeStart)) {
                            return false;
                        }
                        
                        if (now.isAfter(oneHourBeforeEnd) || now.isEqual(oneHourBeforeEnd)) {
                            return false;
                        }
                        
                        // 检查人数是否已满（添加空值检查）
                        if (activity.getCurrentParticipants() == null || activity.getMaxParticipants() == null) {
                            return false;
                        }
                        
                        if (activity.getCurrentParticipants() >= activity.getMaxParticipants()) {
                            return false;
                        }
                        
                        return true;
                    } catch (Exception e) {
                        log.error("过滤可报名活动时出错，活动ID: {}, 错误: {}", activity.getId(), e.getMessage());
                        return false;
                    }
                })
                .collect(Collectors.toList());
            
            // 转换为响应对象
            return registrableActivities.stream()
                .map(activity -> {
                    try {
                        ActivityDetailResponse detail = new ActivityDetailResponse();
                        BeanUtils.copyProperties(activity, detail);
                        detail.setIsRegistered(false);
                        
                        // 获取组织者信息
                        if (activity.getOrganizerId() != null) {
                            try {
                                User organizer = userService.getById(activity.getOrganizerId());
                                if (organizer != null) {
                                    detail.setOrganizerName(organizer.getName());
                                    detail.setOrganizerTitle("社区管理员");
                                    detail.setOrganizerPhone(organizer.getPhone());
                                    detail.setOrganizerAvatar(organizer.getAvatar());
                                }
                            } catch (Exception e) {
                                log.error("获取组织者信息时出错，活动ID: {}, 组织者ID: {}", activity.getId(), activity.getOrganizerId());
                            }
                        }
                        
                        return detail;
                    } catch (Exception e) {
                        log.error("转换活动详情响应时出错，活动ID: {}", activity.getId(), e);
                        return null;
                    }
                })
                .filter(detail -> detail != null)
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取可报名活动列表失败", e);
            throw new BusinessException(ResultCode.INTERNAL_SERVER_ERROR.getCode(), "获取可报名活动列表失败: " + e.getMessage());
        }
    }
}