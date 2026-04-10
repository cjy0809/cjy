package com.community.center.task;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.community.center.entity.Payment;
import com.community.center.entity.ServiceReservation;
import com.community.center.mapper.PaymentMapper;
import com.community.center.mapper.ServiceReservationMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class PaymentAutoCancelTask {

    private static final Logger logger = LoggerFactory.getLogger(PaymentAutoCancelTask.class);
    
    private static final int TIMEOUT_MINUTES = 5;
    
    @Autowired
    private PaymentMapper paymentMapper;
    
    @Autowired
    private ServiceReservationMapper serviceReservationMapper;

    @Scheduled(fixedRate = 60000)
    public void autoCancelExpiredPayments() {
        try {
            logger.info("开始执行超时支付订单自动取消任务");
            
            LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(TIMEOUT_MINUTES);
            
            QueryWrapper<Payment> paymentWrapper = new QueryWrapper<>();
            paymentWrapper.eq("status", 0);
            paymentWrapper.lt("create_time", fiveMinutesAgo);
            
            List<Payment> expiredPayments = paymentMapper.selectList(paymentWrapper);
            
            if (!expiredPayments.isEmpty()) {
                for (Payment payment : expiredPayments) {
                    logger.info("自动取消超时支付订单: ID={}, 预约ID={}, 创建时间={}", 
                        payment.getId(), payment.getBookingId(), payment.getCreateTime());
                    
                    payment.setStatus(2);
                    paymentMapper.updateById(payment);
                    
                    QueryWrapper<ServiceReservation> reservationWrapper = new QueryWrapper<>();
                    reservationWrapper.eq("id", payment.getBookingId());
                    ServiceReservation reservation = serviceReservationMapper.selectOne(reservationWrapper);
                    
                    if (reservation == null) {
                        logger.warn("未找到对应的预约记录: 预约ID={}", payment.getBookingId());
                        continue;
                    }
                    
                    reservation.setStatus(5);
                    serviceReservationMapper.updateById(reservation);
                    
                    logger.info("已取消预约: 预约ID={}, 状态更新为已取消", reservation.getId());
                }
                
                logger.info("超时支付订单自动取消任务完成，共取消{}条记录", expiredPayments.size());
            } else {
                logger.info("没有需要自动取消的超时支付订单");
            }
        } catch (Exception e) {
            logger.error("执行超时支付订单自动取消任务时发生错误", e);
        }
    }
}
