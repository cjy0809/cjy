package com.community.center.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.community.center.entity.ServiceReservation;
import com.community.center.dto.ServiceReservationDetailDTO;
import java.util.List;

/**
 * 服务预约服务接口
 */
public interface ServiceReservationService extends IService<ServiceReservation> {
    
    /**
     * 分页获取服务预约列表
     */
    IPage<ServiceReservation> getReservationPage(int pageNum, int pageSize, Long userId, Long serviceId, Integer status, String userName);
    
    /**
     * 根据用户ID获取预约列表
     */
    List<ServiceReservation> getReservationsByUserId(Long userId);
    
    /**
     * 创建服务预约
     */
    Long createReservation(ServiceReservation reservation);
    
    /**
     * 更新预约状态
     */
    boolean updateReservationStatus(Long id, Integer status);
    
    /**
     * 更新预约状态并设置服务人员
     */
    boolean updateReservationStatusWithUser(Long id, Integer status, String servicePerson);
    
    /**
     * 取消预约
     */
    boolean cancelReservation(Long id);
    
    /**
     * 删除预约
     */
    boolean deleteReservation(Long id);
    
    /**
     * 根据用户ID删除该用户的所有服务预约记录
     */
    boolean deleteServiceReservationsByUserId(Long userId);
    
    /**
     * 获取用户服务预约详情（包含服务信息）
     */
    List<ServiceReservationDetailDTO> getReservationDetailsByUserId(Long userId);
}
