package com.community.center.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.center.entity.Reservation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 预约Mapper接口
 */
@Mapper
public interface ReservationMapper extends BaseMapper<Reservation> {
    
    /**
     * 分页查询预约列表
     */
    Page<Reservation> selectReservationPage(Page<Reservation> page, @Param("userId") Long userId, @Param("venueId") Long venueId, @Param("venueName") String venueName, @Param("userName") String userName, @Param("status") Integer status, @Param("startTime") String startTime, @Param("endTime") String endTime);
    
    /**
     * 测试预约筛选逻辑
     */
    Integer testReservationFilter(@Param("venueName") String venueName, @Param("userName") String userName);
    
    /**
     * 查询场地在指定时间段的预约列表
     */
    List<Reservation> selectReservationByVenueAndTime(@Param("venueId") Long venueId, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
    
    /**
     * 查询用户在指定时间段的预约列表
     */
    List<Reservation> selectReservationByUserAndTime(@Param("userId") Long userId, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
    
    /**
     * 查询日历视图预约数据
     */
    List<Reservation> selectCalendarReservations(@Param("venueId") Long venueId, @Param("startDate") String startDate, @Param("endDate") String endDate, @Param("status") Integer status);
}