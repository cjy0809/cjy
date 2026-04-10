package com.community.center.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.community.center.entity.Payment;
import com.community.center.entity.ServiceReservation;
import com.community.center.mapper.PaymentMapper;
import com.community.center.mapper.ServiceReservationMapper;
import com.community.center.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
public class PaymentServiceImpl extends ServiceImpl<PaymentMapper, Payment> implements PaymentService {
    
    @Autowired
    private ServiceReservationMapper serviceReservationMapper;
    
    @Override
    public IPage<Payment> getPaymentPage(int pageNum, int pageSize, Long userId, Long serviceId, Integer status) {
        QueryWrapper<Payment> wrapper = new QueryWrapper<>();
        
        if (userId != null) {
            wrapper.eq("user_id", userId);
        }
        
        if (serviceId != null) {
            wrapper.eq("service_id", serviceId);
        }
        
        if (status != null) {
            wrapper.eq("status", status);
        }
        
        wrapper.orderByDesc("create_time");
        
        return baseMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }
    
    @Override
    public List<Payment> getPaymentsByUserId(Long userId) {
        QueryWrapper<Payment> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.orderByDesc("create_time");
        return baseMapper.selectList(wrapper);
    }
    
    @Override
    public Payment getPaymentByBookingId(Long bookingId) {
        QueryWrapper<Payment> wrapper = new QueryWrapper<>();
        wrapper.eq("booking_id", bookingId);
        wrapper.orderByDesc("create_time");
        List<Payment> payments = baseMapper.selectList(wrapper);
        
        if (payments != null && !payments.isEmpty()) {
            return payments.get(0);
        }
        return null;
    }
    
    @Override
    public Payment createPayment(Payment payment) {
        payment.setStatus(0);
        payment.setTransactionId(UUID.randomUUID().toString());
        
        int result = baseMapper.insert(payment);
        if (result > 0) {
            return payment;
        }
        return null;
    }
    
    @Override
    public boolean updatePaymentStatus(Long id, Integer status) {
        Payment payment = new Payment();
        payment.setId(id);
        payment.setStatus(status);
        return baseMapper.updateById(payment) > 0;
    }
    
    @Override
    public boolean cancelPayment(Long bookingId) {
        QueryWrapper<Payment> wrapper = new QueryWrapper<>();
        wrapper.eq("booking_id", bookingId);
        Payment payment = baseMapper.selectOne(wrapper);
        
        if (payment != null) {
            boolean paymentResult = updatePaymentStatus(payment.getId(), 2);
            
            if (paymentResult) {
                QueryWrapper<ServiceReservation> reservationWrapper = new QueryWrapper<>();
                reservationWrapper.eq("id", bookingId);
                ServiceReservation reservation = serviceReservationMapper.selectOne(reservationWrapper);
                
                if (reservation != null) {
                    reservation.setStatus(5);
                    serviceReservationMapper.updateById(reservation);
                }
            }
            
            return paymentResult;
        }
        return false;
    }
    
    @Override
    public boolean deletePayment(Long id) {
        return baseMapper.deleteById(id) > 0;
    }
}
