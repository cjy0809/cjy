package com.community.center.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.community.center.entity.Payment;
import java.util.List;

public interface PaymentService extends IService<Payment> {
    
    IPage<Payment> getPaymentPage(int pageNum, int pageSize, Long userId, Long serviceId, Integer status);
    
    List<Payment> getPaymentsByUserId(Long userId);
    
    Payment getPaymentByBookingId(Long bookingId);
    
    Payment createPayment(Payment payment);
    
    boolean updatePaymentStatus(Long id, Integer status);
    
    boolean cancelPayment(Long bookingId);
    
    boolean deletePayment(Long id);
}
