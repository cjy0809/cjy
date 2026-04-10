package com.community.center.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.center.dto.response.RegistrationListResponse;
import com.community.center.entity.Registration;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 活动报名Mapper接口
 */
@Mapper
public interface RegistrationMapper extends BaseMapper<Registration> {
    
    /**
     * 查询用户是否已报名活动
     */
    Registration selectByUserIdAndActivityId(@Param("userId") Long userId, @Param("activityId") Long activityId);
    
    /**
     * 分页查询报名列表
     */
    Page<RegistrationListResponse> selectRegistrationPage(Page<RegistrationListResponse> page, 
                                                        @Param("activityTitle") String activityTitle, 
                                                        @Param("userName") String userName, 
                                                        @Param("status") Integer status);
    
    /**
     * 检查表结构
     */
    List<Map<String, Object>> checkTableStructure();
    
    /**
     * 测试数据库连接
     */
    Integer testConnection();
}