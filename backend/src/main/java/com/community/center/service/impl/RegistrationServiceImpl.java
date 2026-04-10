package com.community.center.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.community.center.common.ResultCode;
import com.community.center.dto.request.RegistrationCreateRequest;
import com.community.center.dto.request.RegistrationUpdateRequest;
import com.community.center.dto.response.RegistrationDetailResponse;
import com.community.center.dto.response.RegistrationListResponse;
import com.community.center.dto.response.RegistrationStatisticsResponse;
import com.community.center.entity.Activity;
import com.community.center.entity.Registration;
import com.community.center.entity.User;
import com.community.center.exception.BusinessException;
import com.community.center.mapper.ActivityMapper;
import com.community.center.mapper.RegistrationMapper;
import com.community.center.mapper.UserMapper;
import com.community.center.service.RegistrationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 活动报名服务实现类
 */
@Slf4j
@Service
public class RegistrationServiceImpl extends ServiceImpl<RegistrationMapper, Registration> implements RegistrationService {

    @Resource
    private RegistrationMapper registrationMapper;
    
    @Resource
    private ActivityMapper activityMapper;
    
    @Resource
    private UserMapper userMapper;

    @Override
    public void createRegistration(RegistrationCreateRequest request) {
        // 检查活动是否存在
        Activity activity = activityMapper.selectById(request.getActivityId());
        if (activity == null) {
            throw new BusinessException(ResultCode.ACTIVITY_NOT_FOUND);
        }
        
        // 检查用户是否存在
        User user = userMapper.selectById(request.getUserId());
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        
        // 检查是否已报名
        LambdaQueryWrapper<Registration> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Registration::getUserId, request.getUserId())
                   .eq(Registration::getActivityId, request.getActivityId());
        Registration existRegistration = registrationMapper.selectOne(queryWrapper);
        if (existRegistration != null) {
            throw new BusinessException(ResultCode.ALREADY_REGISTERED);
        }
        
        // 检查人数是否已满
        if (activity.getCurrentParticipants() >= activity.getMaxParticipants()) {
            throw new BusinessException(ResultCode.ACTIVITY_FULL);
        }
        
        // 创建报名记录
        Registration registration = new Registration();
        BeanUtils.copyProperties(request, registration);
        registration.setRegistrationTime(LocalDateTime.now());
        registration.setStatus(1); // 默认状态为已报名
        
        // 如果没有设置报名类型，默认为自主报名(0)
        if (registration.getRegistrationType() == null) {
            registration.setRegistrationType(0);
        }
        
        this.save(registration);
        
        // 增加活动当前参与人数
        activityMapper.increaseCurrentParticipants(request.getActivityId());
    }

    @Override
    public void updateRegistration(Long registrationId, RegistrationUpdateRequest request) {
        Registration registration = this.getById(registrationId);
        if (registration == null) {
            throw new BusinessException(ResultCode.REGISTRATION_NOT_FOUND);
        }
        
        // 更新报名信息
        if (StringUtils.hasText(request.getRemark())) {
            registration.setRemark(request.getRemark());
        }
        if (request.getStatus() != null) {
            registration.setStatus(request.getStatus());
        }
        
        this.updateById(registration);
    }

    @Override
    public void deleteRegistration(Long registrationId) {
        Registration registration = this.getById(registrationId);
        if (registration == null) {
            throw new BusinessException(ResultCode.REGISTRATION_NOT_FOUND);
        }
        
        // 删除报名记录
        this.removeById(registrationId);
        
        // 减少活动当前参与人数
        activityMapper.decreaseCurrentParticipants(registration.getActivityId());
    }

    @Override
    public Page<RegistrationListResponse> getRegistrationPage(Integer page, Integer size, String activityTitle, String userName, Integer status) {
        log.info("获取报名列表，参数：page={}, size={}, activityTitle={}, userName={}, status={}", page, size, activityTitle, userName, status);
        
        try {
            // 使用自定义的分页查询方法
            Page<RegistrationListResponse> registrationPage = registrationMapper.selectRegistrationPage(
                new Page<>(page, size), activityTitle, userName, status);
            log.info("数据库查询成功，返回记录数：{}", registrationPage.getRecords().size());
            
            return registrationPage;
        } catch (Exception e) {
            log.error("获取报名列表失败，错误详情：", e);
            log.error("异常类型：{}", e.getClass().getName());
            log.error("异常消息：{}", e.getMessage());
            throw new RuntimeException("获取报名列表失败：" + e.getMessage(), e);
        }
    }

    @Override
    public List<RegistrationListResponse> getUserRegistrations(Long userId) {
        // 检查用户是否存在
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        
        // 查询用户报名记录
        LambdaQueryWrapper<Registration> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Registration::getUserId, userId)
                   .orderByDesc(Registration::getRegistrationTime);
        
        List<Registration> registrations = this.list(queryWrapper);
        
        // 转换为响应对象
        return registrations.stream()
            .map(this::convertToListResponse)
            .collect(Collectors.toList());
    }

    @Override
    public List<RegistrationListResponse> getActivityRegistrations(Long activityId) {
        // 检查活动是否存在
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw new BusinessException(ResultCode.ACTIVITY_NOT_FOUND);
        }
        
        // 查询活动报名记录
        LambdaQueryWrapper<Registration> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Registration::getActivityId, activityId)
                   .orderByDesc(Registration::getRegistrationTime);
        
        List<Registration> registrations = this.list(queryWrapper);
        
        // 转换为响应对象
        return registrations.stream()
            .map(this::convertToListResponse)
            .collect(Collectors.toList());
    }

    @Override
    public void approveRegistration(Long registrationId, Integer status, String remark) {
        Registration registration = this.getById(registrationId);
        if (registration == null) {
            throw new BusinessException(ResultCode.REGISTRATION_NOT_FOUND);
        }
        
        // 验证状态值是否有效 (1:已报名, 2:已通过, 3:已拒绝)
        if (status == null || status < 1 || status > 3) {
            throw new BusinessException(ResultCode.PARAM_VALIDATION_ERROR.getCode(), "无效的报名状态值，允许的值: 1(已报名), 2(已通过), 3(已拒绝)");
        }
        
        // 记录原状态
        Integer oldStatus = registration.getStatus();
        
        // 更新报名状态
        registration.setStatus(status);
        if (StringUtils.hasText(remark)) {
            registration.setRemark(remark);
        }
        
        this.updateById(registration);
        
        // 根据状态变化更新活动参与人数
        // 只有状态为已通过(2)时才计入当前参与人数
        if (oldStatus == 1 || oldStatus == 2) {
            // 原状态是已报名或已通过
            if (status == 3) {
                // 新状态是已拒绝，减少参与人数
                activityMapper.decreaseCurrentParticipants(registration.getActivityId());
            }
        } else if (oldStatus == 3 || oldStatus == 4) {
            // 原状态是已拒绝或已取消
            if (status == 2) {
                // 新状态是已通过，增加参与人数
                activityMapper.increaseCurrentParticipants(registration.getActivityId());
            }
        }
    }

    @Override
    public void cancelRegistration(Long registrationId) {
        Registration registration = this.getById(registrationId);
        if (registration == null) {
            throw new BusinessException(ResultCode.REGISTRATION_NOT_FOUND);
        }
        
        // 更新报名状态为已取消
        registration.setStatus(4); // 4:已取消
        this.updateById(registration);
        
        // 减少活动当前参与人数
        activityMapper.decreaseCurrentParticipants(registration.getActivityId());
    }

    @Override
    public RegistrationStatisticsResponse getRegistrationStatistics() {
        log.info("获取报名统计信息");
        
        try {
            RegistrationStatisticsResponse response = new RegistrationStatisticsResponse();
            
            // 总报名数
            int totalRegistrations = Math.toIntExact(this.count());
            response.setTotalRegistrations(totalRegistrations);
            log.info("总报名数：{}", totalRegistrations);
            
            // 按状态统计
            LambdaQueryWrapper<Registration> queryWrapper;
            
            // 已报名数
            queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Registration::getStatus, 1);
            int pendingCount = Math.toIntExact(this.count(queryWrapper));
            response.setPendingRegistrations(pendingCount);
            log.info("已报名数：{}", pendingCount);
            
            // 已通过数
            queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Registration::getStatus, 2);
            int approvedCount = Math.toIntExact(this.count(queryWrapper));
            response.setApprovedRegistrations(approvedCount);
            log.info("已通过数：{}", approvedCount);
            
            // 已拒绝数
            queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Registration::getStatus, 3);
            int rejectedCount = Math.toIntExact(this.count(queryWrapper));
            response.setRejectedRegistrations(rejectedCount);
            log.info("已拒绝数：{}", rejectedCount);
            
            // 已取消数
            queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Registration::getStatus, 4);
            int cancelledCount = Math.toIntExact(this.count(queryWrapper));
            response.setCancelledRegistrations(cancelledCount);
            log.info("已取消数：{}", cancelledCount);
            
            // 本月报名数
            queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.ge(Registration::getRegistrationTime, LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0));
            int thisMonthCount = Math.toIntExact(this.count(queryWrapper));
            response.setThisMonthRegistrations(thisMonthCount);
            log.info("本月报名数：{}", thisMonthCount);
            
            return response;
        } catch (Exception e) {
            log.error("获取报名统计信息失败，错误详情：", e);
            log.error("异常类型：{}", e.getClass().getName());
            log.error("异常消息：{}", e.getMessage());
            throw new RuntimeException("获取报名统计信息失败：" + e.getMessage(), e);
        }
    }
    
    /**
     * 转换为列表响应对象
     */
    private RegistrationListResponse convertToListResponse(@NonNull Registration registration) {
        RegistrationListResponse response = new RegistrationListResponse();
        BeanUtils.copyProperties(registration, response);
        
        // 获取活动信息
        Activity activity = activityMapper.selectById(registration.getActivityId());
        if (activity != null) {
            response.setActivityTitle(activity.getTitle());
            response.setActivityStartTime(activity.getStartTime());
            response.setActivityEndTime(activity.getEndTime());
        }
        
        // 获取用户信息
        User user = userMapper.selectById(registration.getUserId());
        if (user != null) {
            response.setUserName(user.getName());
            response.setUserPhone(user.getPhone());
        }
        
        return response;
    }
    
    @Override
    public List<RegistrationDetailResponse> getCalendarRegistrations(String month, Long activityId, Integer status) {
        log.info("获取活动预约日历数据，参数：month={}, activityId={}, status={}", month, activityId, status);
        
        // 构建查询条件
        LambdaQueryWrapper<Registration> queryWrapper = new LambdaQueryWrapper<>();
        
        // 根据月份过滤活动开始时间
        if (StringUtils.hasText(month)) {
            // 解析月份，构建开始和结束时间
            String startDate = month + "-01";
            LocalDateTime startTime = LocalDateTime.parse(startDate + "T00:00:00");
            LocalDateTime endTime = startTime.plusMonths(1);
            
            // 子查询：查询开始时间在指定月份内的活动
            LambdaQueryWrapper<Activity> activityQueryWrapper = new LambdaQueryWrapper<>();
            activityQueryWrapper.between(Activity::getStartTime, startTime, endTime);
            List<Long> activityIds = activityMapper.selectList(activityQueryWrapper)
                    .stream()
                    .map(Activity::getId)
                    .collect(Collectors.toList());
            
            if (!activityIds.isEmpty()) {
                queryWrapper.in(Registration::getActivityId, activityIds);
            } else {
                // 如果没有符合条件的活动，返回空列表
                return List.of();
            }
        }
        
        // 根据活动ID过滤
        if (activityId != null) {
            queryWrapper.eq(Registration::getActivityId, activityId);
        }
        
        // 根据状态过滤
        if (status != null) {
            queryWrapper.eq(Registration::getStatus, status);
        }
        
        // 查询报名记录
        List<Registration> registrations = this.list(queryWrapper);
        
        // 转换为响应对象
        return registrations.stream()
                .map(this::convertToRegistrationDetailResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public boolean deleteRegistrationsByUserId(Long userId) {
        LambdaQueryWrapper<Registration> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Registration::getUserId, userId);
        return this.remove(queryWrapper);
    }
    
    @Override
    public boolean deleteRegistrationsByActivityId(Long activityId) {
        LambdaQueryWrapper<Registration> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Registration::getActivityId, activityId);
        return this.remove(queryWrapper);
    }
    
    @Override
    public void batchApproveRegistrations(List<Long> registrationIds, Integer status, String remark) {
        log.info("批量审核报名，参数：registrationIds={}, status={}, remark={}", registrationIds, status, remark);
        
        if (registrationIds == null || registrationIds.isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_VALIDATION_ERROR.getCode(), "报名ID列表不能为空");
        }
        
        // 验证状态值是否有效 (1:已报名, 2:已通过, 3:已拒绝)
        if (status == null || status < 1 || status > 3) {
            throw new BusinessException(ResultCode.PARAM_VALIDATION_ERROR.getCode(), "无效的报名状态值，允许的值: 1(已报名), 2(已通过), 3(已拒绝)");
        }
        
        // 批量更新报名记录
        for (Long registrationId : registrationIds) {
            try {
                Registration registration = this.getById(registrationId);
                if (registration == null) {
                    log.warn("报名记录不存在，ID: {}", registrationId);
                    continue;
                }
                
                // 记录原状态
                Integer oldStatus = registration.getStatus();
                
                // 更新报名状态
                registration.setStatus(status);
                if (StringUtils.hasText(remark)) {
                    registration.setRemark(remark);
                }
                
                this.updateById(registration);
                
                // 根据状态变化更新活动参与人数
                if (oldStatus == 1 || oldStatus == 2) {
                    if (status == 3) {
                        activityMapper.decreaseCurrentParticipants(registration.getActivityId());
                    }
                } else if (oldStatus == 3 || oldStatus == 4) {
                    if (status == 2) {
                        activityMapper.increaseCurrentParticipants(registration.getActivityId());
                    }
                }
                
                log.info("批量审核成功，报名ID: {}, 原状态: {}, 新状态: {}", registrationId, oldStatus, status);
            } catch (Exception e) {
                log.error("批量审核失败，报名ID: {}, 错误: {}", registrationId, e.getMessage());
            }
        }
        
        log.info("批量审核完成，共处理 {} 条记录", registrationIds.size());
    }
    
    /**
     * 将Registration转换为RegistrationDetailResponse
     */
    private RegistrationDetailResponse convertToRegistrationDetailResponse(@NonNull Registration registration) {
        RegistrationDetailResponse response = new RegistrationDetailResponse();
        BeanUtils.copyProperties(registration, response);
        
        // 获取活动信息
        Activity activity = activityMapper.selectById(registration.getActivityId());
        if (activity != null) {
            response.setActivityTitle(activity.getTitle());
            response.setActivityStartTime(activity.getStartTime());
            response.setActivityEndTime(activity.getEndTime());
            response.setActivityLocation(activity.getLocation());
        }
        
        // 获取用户信息
        User user = userMapper.selectById(registration.getUserId());
        if (user != null) {
            response.setUserName(user.getName());
            response.setUserPhone(user.getPhone());
        }
        
        return response;
    }
}