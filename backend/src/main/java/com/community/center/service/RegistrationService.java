package com.community.center.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.community.center.dto.request.RegistrationCreateRequest;
import com.community.center.dto.request.RegistrationUpdateRequest;
import com.community.center.dto.response.RegistrationDetailResponse;
import com.community.center.dto.response.RegistrationListResponse;
import com.community.center.dto.response.RegistrationStatisticsResponse;
import com.community.center.entity.Registration;

import java.util.List;

/**
 * 活动报名服务接口
 */
public interface RegistrationService extends IService<Registration> {
    
    /**
     * 创建报名记录
     */
    void createRegistration(RegistrationCreateRequest request);
    
    /**
     * 更新报名记录
     */
    void updateRegistration(Long registrationId, RegistrationUpdateRequest request);
    
    /**
     * 删除报名记录
     */
    void deleteRegistration(Long registrationId);
    
    /**
     * 分页查询报名列表
     */
    Page<RegistrationListResponse> getRegistrationPage(Integer page, Integer size, String activityTitle, String userName, Integer status);
    
    /**
     * 获取用户报名记录
     */
    List<RegistrationListResponse> getUserRegistrations(Long userId);
    
    /**
     * 获取活动报名记录
     */
    List<RegistrationListResponse> getActivityRegistrations(Long activityId);
    
    /**
     * 审核报名
     */
    void approveRegistration(Long registrationId, Integer status, String remark);
    
    /**
     * 取消报名
     */
    void cancelRegistration(Long registrationId);
    
    /**
     * 获取报名统计信息
     */
    RegistrationStatisticsResponse getRegistrationStatistics();
    
    /**
     * 获取活动预约日历数据
     */
    List<RegistrationDetailResponse> getCalendarRegistrations(String month, Long activityId, Integer status);
    
    /**
     * 根据用户ID删除该用户的所有活动报名记录
     */
    boolean deleteRegistrationsByUserId(Long userId);
    
    /**
     * 根据活动ID删除该活动的所有报名记录
     */
    boolean deleteRegistrationsByActivityId(Long activityId);
    
    /**
     * 批量审核报名
     */
    void batchApproveRegistrations(List<Long> registrationIds, Integer status, String remark);
}