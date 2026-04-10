package com.community.center.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.community.center.common.ResultCode;
import com.community.center.dto.request.ReservationCreateRequest;
import com.community.center.dto.response.ReservationResponse;
import com.community.center.entity.Reservation;
import com.community.center.entity.User;
import com.community.center.entity.Venue;
import com.community.center.exception.BusinessException;
import com.community.center.mapper.ReservationMapper;
import com.community.center.mapper.UserMapper;
import com.community.center.mapper.VenueMapper;
import com.community.center.service.ReservationService;
import com.community.center.controller.VenueWebSocketController;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 预约服务实现类
 */
@Service
public class ReservationServiceImpl extends ServiceImpl<ReservationMapper, Reservation> implements ReservationService {

    @Resource
    private UserMapper userMapper;
    
    @Resource
    private VenueMapper venueMapper;
    
    @Autowired
    private VenueWebSocketController webSocketController;

    @Override
    public ReservationResponse getReservationDetail(Long reservationId) {
        Reservation reservation = this.getById(reservationId);
        if (reservation == null) {
            throw new BusinessException(ResultCode.RESERVATION_NOT_FOUND);
        }
        
        return convertToResponse(reservation);
    }

    @Override
    public Page<ReservationResponse> getReservationPage(Integer page, Integer size, Long userId, Long venueId, String venueName, String userName, Integer status, String startTime, String endTime) {
        // 添加调试日志
        System.out.println("服务层接收参数 - venueName: " + venueName + ", userName: " + userName + ", status: " + status);
        
        // 测试筛选逻辑
        if ((venueName != null && !venueName.isEmpty()) || (userName != null && !userName.isEmpty())) {
            Integer testCount = baseMapper.testReservationFilter(venueName, userName);
            System.out.println("测试筛选结果数量: " + testCount);
        }
        
        Page<Reservation> reservationPage = new Page<>(page, size);
        // 调用自定义的selectReservationPage方法，该方法会自动填充page对象
        baseMapper.selectReservationPage(reservationPage, userId, venueId, venueName, userName, status, startTime, endTime);
        
        // 添加查询结果日志
        System.out.println("查询结果总数: " + reservationPage.getTotal());
        
        // 转换为响应对象
        Page<ReservationResponse> resultPage = new Page<>(page, size);
        resultPage.setTotal(reservationPage.getTotal());
        
        List<ReservationResponse> responseList = reservationPage.getRecords().stream()
            .map(this::convertToResponse)
            .collect(java.util.stream.Collectors.toList());
        
        resultPage.setRecords(responseList);
        
        return resultPage;
    }
    
    @Override
    public Boolean createReservation(@NonNull ReservationCreateRequest request) {
        Reservation reservation = new Reservation();
        BeanUtils.copyProperties(request, reservation);
        reservation.setStatus(0); // 待审核
        
        boolean result = this.save(reservation);
        
        // 发送 WebSocket 通知
        if (result) {
            // 创建包含详细预约信息的消息
            Map<String, Object> reservationData = new HashMap<>();
            reservationData.put("id", reservation.getId());
            reservationData.put("venueId", reservation.getVenueId());
            reservationData.put("userId", reservation.getUserId());
            reservationData.put("reservationDate", reservation.getReservationDate());
            reservationData.put("startTime", reservation.getStartTime());
            reservationData.put("endTime", reservation.getEndTime());
            reservationData.put("participants", reservation.getParticipants());
            reservationData.put("status", reservation.getStatus());
            
            webSocketController.sendReservationUpdate(request.getVenueId(), reservationData);
        }
        
        return result;
    }
    
    @Override
    public Boolean updateReservation(Long id, @NonNull ReservationCreateRequest request) {
        Reservation reservation = this.getById(id);
        if (reservation == null) {
            throw new BusinessException(ResultCode.RESERVATION_NOT_FOUND);
        }
        
        BeanUtils.copyProperties(request, reservation);
        return this.updateById(reservation);
    }
    
    @Override
    public Boolean deleteReservation(Long id) {
        return this.removeById(id);
    }

    @Override
    public void cancelReservation(Long reservationId) {
        Long currentUserId = getCurrentUserId();
        
        // 检查预约记录是否存在
        Reservation reservation = this.getById(reservationId);
        if (reservation == null) {
            throw new BusinessException(ResultCode.RESERVATION_NOT_FOUND);
        }
        
        // 检查是否是当前用户的预约或者是管理员
        if (!reservation.getUserId().equals(currentUserId) && !isAdmin()) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        
        // 检查预约状态
        if (reservation.getStatus() == 2) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "预约已取消");
        }
        
        // 更新预约状态为已取消
        reservation.setStatus(2);
        boolean result = this.updateById(reservation);
        
        // 发送 WebSocket 通知
        if (result) {
            // 创建包含详细预约信息的消息
            Map<String, Object> reservationData = new HashMap<>();
            reservationData.put("id", reservation.getId());
            reservationData.put("venueId", reservation.getVenueId());
            reservationData.put("userId", reservation.getUserId());
            reservationData.put("reservationDate", reservation.getReservationDate());
            reservationData.put("startTime", reservation.getStartTime());
            reservationData.put("endTime", reservation.getEndTime());
            reservationData.put("participants", reservation.getParticipants());
            reservationData.put("status", reservation.getStatus());
            
            webSocketController.sendReservationUpdate(reservation.getVenueId(), reservationData);
        }
    }
    
    @Override
    public Boolean reviewReservation(Long id, Integer status, String comment, Long reviewerId) {
        Reservation reservation = this.getById(id);
        if (reservation == null) {
            throw new BusinessException(ResultCode.RESERVATION_NOT_FOUND);
        }
        
        reservation.setStatus(status);
        reservation.setReviewComment(comment);
        reservation.setReviewerId(reviewerId);
        reservation.setReviewTime(LocalDateTime.now());
        
        boolean result = this.updateById(reservation);
        
        // 发送 WebSocket 通知
        if (result) {
            // 创建包含详细预约信息的消息
            Map<String, Object> reservationData = new HashMap<>();
            reservationData.put("id", reservation.getId());
            reservationData.put("venueId", reservation.getVenueId());
            reservationData.put("userId", reservation.getUserId());
            reservationData.put("reservationDate", reservation.getReservationDate());
            reservationData.put("startTime", reservation.getStartTime());
            reservationData.put("endTime", reservation.getEndTime());
            reservationData.put("participants", reservation.getParticipants());
            reservationData.put("status", reservation.getStatus());
            
            webSocketController.sendReservationUpdate(reservation.getVenueId(), reservationData);
        }
        
        return result;
    }

    @Override
    public void cancelExpiredReservations() {
        // 查询所有已过期的预约
        LambdaQueryWrapper<Reservation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.lt(Reservation::getEndTime, LocalDateTime.now())
                   .eq(Reservation::getStatus, 1);
        
        // 批量更新状态为已取消
        Reservation updateEntity = new Reservation();
        updateEntity.setStatus(2);
        
        this.update(updateEntity, queryWrapper);
    }
    
    @Override
    public List<ReservationResponse> getCalendarReservations(String month, Long venueId, Integer status) {
        // 计算月份的开始和结束日期
        LocalDate startDate = LocalDate.parse(month + "-01");
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);
        
        // 查询该日期范围内的所有预约
        List<Reservation> reservations = baseMapper.selectCalendarReservations(
                venueId, 
                startDate.toString(), 
                endDate.toString(), 
                status
        );
        
        // 转换为响应对象
        return reservations.stream()
                .map(this::convertToResponse)
                .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * 将Reservation实体转换为ReservationResponse
     */
    private ReservationResponse convertToResponse(@NonNull Reservation reservation) {
        ReservationResponse response = new ReservationResponse();
        BeanUtils.copyProperties(reservation, response);
        
        // 获取用户名
        User user = userMapper.selectById(reservation.getUserId());
        if (user != null) {
            response.setUserName(user.getName());
        } else {
            response.setUserName("未知用户");
        }
        
        // 获取场地名称
        Venue venue = venueMapper.selectById(reservation.getVenueId());
        if (venue != null) {
            response.setVenueName(venue.getName());
        } else {
            response.setVenueName("未知场地");
        }
        
        // 获取审核人名称
        if (reservation.getReviewerId() != null) {
            User reviewer = userMapper.selectById(reservation.getReviewerId());
            if (reviewer != null) {
                response.setReviewerName(reviewer.getName());
            } else {
                response.setReviewerName("未知审核人");
            }
        }
        
        // 设置参与人数
        System.out.println("DEBUG: Reservation ID " + reservation.getId() + " - Participants from DB: " + reservation.getParticipants());
        if (reservation.getParticipants() != null) {
            response.setParticipants(reservation.getParticipants());
            System.out.println("DEBUG: Set participants to response: " + reservation.getParticipants());
        } else {
            response.setParticipants(1); // 默认值为1
            System.out.println("DEBUG: Participants was null, set default value: 1");
        }
        
        // 设置状态名称
        switch (reservation.getStatus()) {
            case 0:
                response.setStatusName("待审核");
                break;
            case 1:
                response.setStatusName("已通过");
                break;
            case 2:
                response.setStatusName("已拒绝");
                break;
            case 3:
                response.setStatusName("已取消");
                break;
            default:
                response.setStatusName("未知状态");
        }
        
        return response;
    }
    
    @Override
    public boolean deleteReservationsByUserId(Long userId) {
        LambdaQueryWrapper<Reservation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Reservation::getUserId, userId);
        return this.remove(queryWrapper);
    }
    
    @Override
    public Map<String, Object> getVenueReservationStatistics(Long venueId, LocalDate startDate, LocalDate endDate) {
        // 设置默认日期范围
        if (startDate == null) {
            startDate = LocalDate.now();
        }
        if (endDate == null) {
            endDate = startDate.plusDays(7);
        }
        
        // 查询指定场地和日期范围内的预约记录
        LambdaQueryWrapper<Reservation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Reservation::getVenueId, venueId)
                   .between(Reservation::getReservationDate, startDate, endDate)
                   .eq(Reservation::getStatus, 1); // 只统计已通过的预约
        
        List<Reservation> reservations = this.list(queryWrapper);
        
        // 统计数据
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalReservations", reservations.size());
        statistics.put("venueId", venueId);
        statistics.put("startDate", startDate);
        statistics.put("endDate", endDate);
        
        // 按日期统计预约人数
        Map<String, Integer> dailyCount = new HashMap<>();
        Map<String, Integer> dailyParticipants = new HashMap<>();
        for (Reservation reservation : reservations) {
            String date = reservation.getReservationDate().toString();
            // 统计预约数量
            dailyCount.put(date, dailyCount.getOrDefault(date, 0) + 1);
            // 统计参与人数
            int participants = reservation.getParticipants() != null ? reservation.getParticipants() : 1;
            dailyParticipants.put(date, dailyParticipants.getOrDefault(date, 0) + participants);
        }
        statistics.put("dailyCount", dailyCount);
        statistics.put("dailyParticipants", dailyParticipants);
        
        // 按时间段统计预约人数
        Map<String, Integer> timeSlotCount = new HashMap<>();
        Map<String, Integer> timeSlotParticipants = new HashMap<>();
        for (Reservation reservation : reservations) {
            String startTime = reservation.getStartTime().toString();
            String timeSlot = getTimeSlotLabel(startTime);
            // 统计预约数量
            timeSlotCount.put(timeSlot, timeSlotCount.getOrDefault(timeSlot, 0) + 1);
            // 统计参与人数
            int participants = reservation.getParticipants() != null ? reservation.getParticipants() : 1;
            timeSlotParticipants.put(timeSlot, timeSlotParticipants.getOrDefault(timeSlot, 0) + participants);
        }
        statistics.put("timeSlotCount", timeSlotCount);
        statistics.put("timeSlotParticipants", timeSlotParticipants);
        
        // 获取场地信息
        Venue venue = venueMapper.selectById(venueId);
        if (venue != null) {
            statistics.put("venueName", venue.getName());
            statistics.put("venueCapacity", venue.getCapacity());
        }
        
        return statistics;
    }
    
    /**
     * 根据时间获取时间段标签
     */
    private String getTimeSlotLabel(String timeString) {
        try {
            String[] parts = timeString.split(":");
            if (parts.length >= 1) {
                int hour = Integer.parseInt(parts[0]);
                
                if (hour >= 6 && hour < 12) {
                    return "上午";
                } else if (hour >= 12 && hour < 18) {
                    return "下午";
                } else {
                    return "晚上";
                }
            }
        } catch (NumberFormatException e) {
            // 忽略解析错误
        }
        return "未知";
    }
    
    /**
     * 获取当前用户ID
     */
    private Long getCurrentUserId() {
        // 临时实现：返回默认用户ID
        // 后续需要从SecurityContext中获取真实的当前用户ID
        return 1L;
    }
    
    /**
     * 检查当前用户是否是管理员
     */
    private boolean isAdmin() {
        // 临时实现：默认返回true
        // 后续需要从SecurityContext中获取真实的用户角色
        return true;
    }
}