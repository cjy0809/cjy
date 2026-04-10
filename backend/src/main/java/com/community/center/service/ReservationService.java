package com.community.center.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.community.center.dto.request.ReservationCreateRequest;
import com.community.center.dto.response.ReservationResponse;
import com.community.center.entity.Reservation;
import org.springframework.lang.NonNull;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 预约服务接口
 */
public interface ReservationService extends IService<Reservation> {
    
    /**
     * 获取预约详情
     */
    ReservationResponse getReservationDetail(Long reservationId);
    
    /**
     * 分页查询预约
     */
    Page<ReservationResponse> getReservationPage(Integer page, Integer size, Long userId, Long venueId, String venueName, String userName, Integer status, String startTime, String endTime);
    
    /**
     * 创建预约
     */
    Boolean createReservation(@NonNull ReservationCreateRequest request);
    
    /**
     * 更新预约
     */
    Boolean updateReservation(Long id, @NonNull ReservationCreateRequest request);
    
    /**
     * 删除预约
     */
    Boolean deleteReservation(Long id);
    
    /**
     * 取消预约
     */
    void cancelReservation(Long reservationId);
    
    /**
     * 审核预约
     */
    Boolean reviewReservation(Long id, Integer status, String comment, Long reviewerId);
    
    /**
     * 批量取消过期预约
     */
    void cancelExpiredReservations();
    
    /**
     * 获取日历视图预约数据
     */
    List<ReservationResponse> getCalendarReservations(String month, Long venueId, Integer status);
    
    /**
     * 根据用户ID删除该用户的所有场地预约记录
     */
    boolean deleteReservationsByUserId(Long userId);
    
    /**
     * 获取场地预约人数统计
     * @param venueId 场地ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 统计数据
     */
    Map<String, Object> getVenueReservationStatistics(Long venueId, LocalDate startDate, LocalDate endDate);
}