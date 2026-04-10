package com.community.center.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.center.entity.Activity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 活动Mapper接口
 */
@Mapper
public interface ActivityMapper extends BaseMapper<Activity> {
    
    /**
     * 分页查询活动列表
     */
    Page<Activity> selectActivityPage(Page<Activity> page, @Param("title") String title, @Param("status") Integer status);
    
    /**
     * 增加活动当前参与人数
     */
    int increaseCurrentParticipants(@Param("activityId") Long activityId);
    
    /**
     * 减少活动当前参与人数
     */
    int decreaseCurrentParticipants(@Param("activityId") Long activityId);
}