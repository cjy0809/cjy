package com.community.center.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.community.center.entity.ServiceReservation;
import com.community.center.dto.ServiceReservationDetailDTO;
import com.community.center.mapper.ServiceReservationMapper;
import com.community.center.mapper.ServiceItemMapper;
import com.community.center.service.ServiceReservationService;
import com.community.center.entity.ServiceItem;
import com.community.center.common.SecurityUtil;
import com.community.center.entity.User;
import com.community.center.service.UserService;
import com.community.center.entity.Payment;
import com.community.center.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 服务预约服务实现类
 */
@Service
public class ServiceReservationServiceImpl extends ServiceImpl<ServiceReservationMapper, ServiceReservation> implements ServiceReservationService {
    
    @Autowired
    private ServiceItemMapper serviceItemMapper;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private PaymentService paymentService;
    
    /**
     * 获取当前登录用户信息
     */
    private User getCurrentUser() {
        Long userId = SecurityUtil.getCurrentUserId();
        return userService.getById(userId);
    }
    
    @Override
    public IPage<ServiceReservation> getReservationPage(int pageNum, int pageSize, Long userId, Long serviceId, Integer status, String userName) {
        QueryWrapper<ServiceReservation> wrapper = new QueryWrapper<>();
        
        if (userId != null) {
            wrapper.eq("user_id", userId);
        }
        
        if (serviceId != null) {
            wrapper.eq("service_id", serviceId);
        }
        
        if (status != null) {
            wrapper.eq("status", status);
        }
        
        if (userName != null && !userName.trim().isEmpty()) {
            QueryWrapper<User> userWrapper = new QueryWrapper<>();
            userWrapper.like("name", userName.trim());
            List<User> users = userService.list(userWrapper);
            
            if (users.isEmpty()) {
                return new Page<>(pageNum, pageSize);
            }
            
            List<Long> userIds = users.stream()
                    .map(User::getId)
                    .collect(Collectors.toList());
            
            wrapper.in("user_id", userIds);
        }
        
        wrapper.orderByDesc("create_time");
        
        return baseMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }
    
    @Override
    public List<ServiceReservation> getReservationsByUserId(Long userId) {
        QueryWrapper<ServiceReservation> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.orderByDesc("create_time");
        return baseMapper.selectList(wrapper);
    }
    
    @Override
    public Long createReservation(ServiceReservation reservation) {
        reservation.setStatus(0);
        int result = baseMapper.insert(reservation);
        if (result > 0) {
            Long reservationId = reservation.getId();
            
            try {
                ServiceItem serviceItem = serviceItemMapper.selectById(reservation.getServiceId());
                if (serviceItem != null) {
                    Payment payment = new Payment();
                    payment.setBookingId(reservationId);
                    payment.setServiceId(reservation.getServiceId());
                    payment.setUserId(reservation.getUserId());
                    payment.setPaymentMethod("2");
                    payment.setAmount(serviceItem.getPrice());
                    payment.setRemark("预约服务支付");
                    
                    paymentService.createPayment(payment);
                    System.out.println("预约创建成功，已自动创建支付记录，预约ID: " + reservationId);
                }
            } catch (Exception e) {
                System.err.println("创建支付记录失败: " + e.getMessage());
                e.printStackTrace();
            }
            
            return reservationId;
        }
        return null;
    }
    
    @Override
    public boolean updateReservationStatus(Long id, Integer status) {
        ServiceReservation existingReservation = baseMapper.selectById(id);
        if (existingReservation == null) {
            return false;
        }
        
        ServiceReservation updateReservation = new ServiceReservation();
        updateReservation.setId(id);
        updateReservation.setStatus(status);
        
        User currentUser = getCurrentUser();
        if (existingReservation.getServicePerson() != null && !existingReservation.getServicePerson().isEmpty()) {
            updateReservation.setServicePerson(existingReservation.getServicePerson());
        } else if (currentUser != null) {
            updateReservation.setServicePerson(currentUser.getName() != null ? currentUser.getName() : currentUser.getUsername());
        }
        
        return baseMapper.updateById(updateReservation) > 0;
    }
    
    @Override
    public boolean updateReservationStatusWithUser(Long id, Integer status, String servicePerson) {
        // 先获取原有预约记录
        ServiceReservation existingReservation = baseMapper.selectById(id);
        if (existingReservation == null) {
            return false;
        }
        
        ServiceReservation updateReservation = new ServiceReservation();
        updateReservation.setId(id);
        updateReservation.setStatus(status);
        updateReservation.setServicePerson(servicePerson);
        
        return baseMapper.updateById(updateReservation) > 0;
    }
    
    @Override
    public boolean cancelReservation(Long id) {
        System.out.println("=== 开始取消预约并退款 ===");
        System.out.println("预约ID: " + id);
        
        try {
            ServiceReservation reservation = this.getById(id);
            if (reservation == null) {
                System.out.println("取消预约失败：预约不存在");
                return false;
            }
            System.out.println("预约状态: " + reservation.getStatus());
            if (reservation.getStatus() != 1 && reservation.getStatus() != 2) {
                System.out.println("取消预约失败：预约状态不是待确认或已确认，无需退款");
                return updateReservationStatus(id, 5);
            }
            QueryWrapper<Payment> paymentWrapper = new QueryWrapper<>();
            paymentWrapper.eq("booking_id", id);
            paymentWrapper.eq("status", 1); // 已支付
            Payment payment = paymentService.getOne(paymentWrapper);
            
            if (payment == null) {
                System.out.println("取消预约失败：未找到已支付的支付记录");
                return updateReservationStatus(id, 5);
            }
            
            System.out.println("找到支付记录: " + payment.getId());
            System.out.println("支付金额: " + payment.getAmount());
            System.out.println("支付方式: " + payment.getPaymentMethod());
            
            if (!"1".equals(payment.getPaymentMethod())) {
                System.out.println("取消预约失败：非余额支付，暂不支持自动退款");
                // 更新支付状态为已退款
                paymentService.updatePaymentStatus(payment.getId(), 3); // 3: 已退款
                return updateReservationStatus(id, 5);
            }
            User user = userService.getById(reservation.getUserId());
            if (user == null) {
                System.out.println("取消预约失败：用户不存在");
                return false;
            }
            
            System.out.println("用户当前余额: " + user.getBalance());
            
            // 6. 退款到用户余额
            user.setBalance(user.getBalance().add(payment.getAmount()));
            boolean balanceUpdated = userService.updateById(user);
            
            if (!balanceUpdated) {
                System.out.println("取消预约失败：更新用户余额失败");
                return false;
            }
            
            System.out.println("退款成功，更新后余额: " + user.getBalance());
            
            // 7. 更新支付状态为已退款
            boolean paymentUpdated = paymentService.updatePaymentStatus(payment.getId(), 3); // 3: 已退款
            
            if (!paymentUpdated) {
                System.out.println("取消预约失败：更新支付状态失败");
                // 回滚余额
                user.setBalance(user.getBalance().subtract(payment.getAmount()));
                userService.updateById(user);
                return false;
            }
            
            System.out.println("支付状态已更新为已退款");
            
            // 8. 更新预约状态为已取消
            boolean reservationUpdated = updateReservationStatus(id, 5); // 5: 已取消
            
            if (!reservationUpdated) {
                System.out.println("取消预约失败：更新预约状态失败");
                // 回滚余额和支付状态
                user.setBalance(user.getBalance().subtract(payment.getAmount()));
                userService.updateById(user);
                paymentService.updatePaymentStatus(payment.getId(), 1); // 恢复为已支付
                return false;
            }
            
            System.out.println("预约状态已更新为已取消");
            System.out.println("=== 取消预约并退款完成 ===");
            return true;
            
        } catch (Exception e) {
            System.err.println("取消预约异常: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public boolean deleteReservation(Long id) {
        try {
            QueryWrapper<Payment> paymentWrapper = new QueryWrapper<>();
            paymentWrapper.eq("booking_id", id);
            paymentService.remove(paymentWrapper);
            System.out.println("删除预约ID " + id + " 对应的支付记录成功");
            
            boolean result = baseMapper.deleteById(id) > 0;
            if (result) {
                System.out.println("删除预约成功，ID: " + id);
            }
            return result;
        } catch (Exception e) {
            System.err.println("删除预约失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public boolean deleteServiceReservationsByUserId(Long userId) {
        QueryWrapper<ServiceReservation> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        return baseMapper.delete(wrapper) >= 0;
    }
    
    @Override
    public List<ServiceReservationDetailDTO> getReservationDetailsByUserId(Long userId) {
        // 1. 获取用户的所有预约
        QueryWrapper<ServiceReservation> reservationWrapper = new QueryWrapper<>();
        reservationWrapper.eq("user_id", userId);
        reservationWrapper.orderByDesc("create_time");
        List<ServiceReservation> reservations = baseMapper.selectList(reservationWrapper);
        
        if (reservations == null || reservations.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 2. 获取所有相关的服务ID
        List<Long> serviceIds = reservations.stream()
                .map(ServiceReservation::getServiceId)
                .collect(Collectors.toList());
        
        // 3. 批量查询服务信息
        final Map<Long, ServiceItem> serviceMap;
        if (!serviceIds.isEmpty()) {
            QueryWrapper<ServiceItem> serviceWrapper = new QueryWrapper<>();
            serviceWrapper.in("id", serviceIds);
            List<ServiceItem> services = serviceItemMapper.selectList(serviceWrapper);
            
            // 将服务信息转换为Map，方便查找
            serviceMap = services.stream()
                    .collect(Collectors.toMap(ServiceItem::getId, service -> service));
        } else {
            serviceMap = new HashMap<>();
        }
        
        // 4. 组装预约详情DTO
        return reservations.stream()
                .map(reservation -> {
                    ServiceReservationDetailDTO dto = new ServiceReservationDetailDTO();
                    // 复制预约基本信息
                    dto.setId(reservation.getId());
                    dto.setServiceId(reservation.getServiceId());
                    dto.setUserId(reservation.getUserId());
                    dto.setReservationDate(reservation.getReservationDate());
                    dto.setReservationTime(reservation.getReservationTime());
                    dto.setStatus(reservation.getStatus());
                    dto.setRemark(reservation.getRemark());
                    dto.setServicePerson(reservation.getServicePerson());
                    dto.setCreateTime(reservation.getCreateTime());
                    dto.setUpdateTime(reservation.getUpdateTime());
                    
                    // 添加服务详情信息
                    ServiceItem service = serviceMap.get(reservation.getServiceId());
                    if (service != null) {
                        dto.setServiceName(service.getName());
                        dto.setServiceDescription(service.getDescription());
                        dto.setServicePrice(service.getPrice());
                        dto.setServiceDuration(service.getDuration());
                        dto.setServiceCategoryName(service.getCategoryName());
                        // ServiceItem实体中没有imageUrl字段，暂时留空
                        dto.setServiceImageUrl("");
                    }
                    
                    return dto;
                })
                .collect(Collectors.toList());
    }
}
