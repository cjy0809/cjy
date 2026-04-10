package com.community.center.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.community.center.dto.request.ActivityCreateRequest;
import com.community.center.dto.request.ActivityUpdateRequest;
import com.community.center.dto.response.ActivityDetailResponse;
import com.community.center.entity.Activity;

import java.util.List;
import java.util.Map;

/**
 * 活动服务接口
 * 定义社区中心活动相关的业务操作，包括活动创建、更新、删除、报名等
 */
public interface ActivityService extends IService<Activity> {
    
    /**
     * 创建活动
     * @param request 创建活动请求，包含活动基本信息
     */
    void createActivity(ActivityCreateRequest request);
    
    /**
     * 更新活动信息
     * @param activityId 活动ID
     * @param request 更新请求，包含需要更新的活动信息
     */
    void updateActivity(Long activityId, ActivityUpdateRequest request);
    
    /**
     * 删除活动
     * @param activityId 活动ID
     */
    void deleteActivity(Long activityId);
    
    /**
     * 分页查询活动列表
     * @param page 页码
     * @param size 每页大小
     * @param title 活动标题（可选，用于模糊查询）
     * @param status 活动状态（可选）
     * @return 分页结果
     */
    Page<ActivityDetailResponse> getActivityPage(Integer page, Integer size, String title, String status);
    
    /**
     * 获取活动详情
     * @param activityId 活动ID
     * @return 活动详情响应对象
     */
    ActivityDetailResponse getActivityDetail(Long activityId);
    
    /**
     * 用户报名参加活动
     * @param activityId 活动ID
     */
    void registerActivity(Long activityId);
    
    /**
     * 取消活动报名
     * @param activityId 活动ID
     */
    void cancelRegistration(Long activityId);
    
    /**
     * 更新活动状态
     * @param activityId 活动ID
     * @param status 新状态
     */
    void updateActivityStatus(Long activityId, Integer status);
    
    /**
     * 获取相关活动推荐
     * @param activityId 活动ID
     * @param limit 返回数量限制
     * @return 相关活动列表
     */
    List<ActivityDetailResponse> getRelatedActivities(Long activityId, Integer limit);
    
    /**
     * 删除指定用户组织的所有活动
     * @param organizerId 组织者ID
     */
    void deleteActivitiesByOrganizerId(Long organizerId);
    
    /**
     * 获取活动统计数据
     * @return 统计信息Map，包含活动总数、参与人数等
     */
    Map<String, Object> getActivityStatistics();
    
    /**
     * 获取可报名的活动列表
     * @return 可报名的活动列表
     */
    List<ActivityDetailResponse> getRegistrableActivities();
}
