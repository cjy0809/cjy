package com.community.center.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.community.center.common.PageResult;
import com.community.center.common.Result;
import com.community.center.dto.PageQueryDTO;
import com.community.center.dto.ServiceReservationDetailDTO;
import com.community.center.entity.ServiceReservation;
import com.community.center.entity.User;
import com.community.center.service.ServiceReservationService;
import com.community.center.service.UserService;
import com.community.center.common.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 服务预约Controller
 */
@RestController
@RequestMapping("/api/service-reservation")
public class ServiceReservationController {
    
    @Autowired
    private ServiceReservationService serviceReservationService;
    
    @Autowired
    private UserService userService;
    
    /**
     * 获取当前登录用户信息
     */
    private User getCurrentUser() {
        Long userId = SecurityUtil.getCurrentUserId();
        return userService.getById(userId);
    }
    
    /**
     * 分页获取服务预约列表
     */
    @GetMapping("/page")
    public Result<PageResult<ServiceReservation>> getReservationPage(PageQueryDTO query) {
        IPage<ServiceReservation> page = serviceReservationService.getReservationPage(
                query.getPageNum(), 
                query.getPageSize(), 
                query.getUserId(), 
                query.getServiceId(), 
                query.getStatus(),
                query.getUserName());
        PageResult<ServiceReservation> pageResult = new PageResult<>(
                200,
                "success",
                page.getTotal(),
                page.getRecords()
        );
        return Result.success(pageResult);
    }
    
    /**
     * 获取所有服务预约
     */
    @GetMapping
    public List<ServiceReservation> getAllReservations() {
        // 这里可以调用一个获取所有服务预约的方法，为简化实现，使用分页查询第一页
        IPage<ServiceReservation> page = serviceReservationService.getReservationPage(1, 100, null, null, null, null);
        return page.getRecords();
    }
    
    /**
     * 根据用户ID获取预约列表
     */
    @GetMapping("/user/{userId}")
    public List<ServiceReservation> getReservationsByUserId(@PathVariable Long userId) {
        return serviceReservationService.getReservationsByUserId(userId);
    }
    
    /**
     * 获取用户服务预约详情（包含服务信息）
     */
    @GetMapping("/user/{userId}/details")
    public Result<List<ServiceReservationDetailDTO>> getReservationDetailsByUserId(@PathVariable Long userId) {
        List<ServiceReservationDetailDTO> details = serviceReservationService.getReservationDetailsByUserId(userId);
        return Result.success(details);
    }
    
    /**
     * 创建服务预约
     */
    @PostMapping
    public Result<Long> createReservation(@RequestBody ServiceReservation reservation) {
        System.out.println("创建服务预约请求: " + reservation);
        Long reservationId = serviceReservationService.createReservation(reservation);
        System.out.println("创建服务预约返回ID: " + reservationId);
        if (reservationId != null) {
            Result<Long> result = Result.success(reservationId);
            System.out.println("返回结果: " + result);
            return result;
        }
        return Result.error(500, "Failed to create reservation");
    }
    
    /**
     * 更新服务预约
     */
    @PutMapping("/{id}")
    public boolean updateReservation(@PathVariable Long id, @RequestBody ServiceReservation reservation) {
        // 设置服务人员为当前登录用户
        User currentUser = getCurrentUser();
        if (currentUser != null) {
            reservation.setServicePerson(currentUser.getName() != null ? currentUser.getName() : currentUser.getUsername());
        }
        reservation.setId(id);
        return serviceReservationService.updateById(reservation);
    }
    
    /**
     * 更新预约状态
     */
    @PutMapping("/{id}/status")
    public boolean updateReservationStatus(@PathVariable Long id, @RequestParam Integer status) {
        // 获取当前登录用户信息
        User currentUser = getCurrentUser();
        if (currentUser != null) {
            return serviceReservationService.updateReservationStatusWithUser(id, status, 
                currentUser.getName() != null ? currentUser.getName() : currentUser.getUsername());
        } else {
            return serviceReservationService.updateReservationStatus(id, status);
        }
    }
    
    /**
     * 取消预约
     */
    @PutMapping("/{id}/cancel")
    public boolean cancelReservation(@PathVariable Long id) {
        return serviceReservationService.cancelReservation(id);
    }
    
    /**
     * 删除预约
     */
    @DeleteMapping("/{id}")
    public boolean deleteReservation(@PathVariable Long id) {
        return serviceReservationService.deleteReservation(id);
    }
}
