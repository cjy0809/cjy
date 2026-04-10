package com.community.center.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.community.center.dto.RechargeRequest;
import com.community.center.entity.Recharge;
import com.community.center.mapper.RechargeMapper;
import com.community.center.service.RechargeService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class RechargeServiceImpl extends ServiceImpl<RechargeMapper, Recharge> implements RechargeService {
    
    @Override
    public IPage<Recharge> getRechargePage(Page<Recharge> page, Long userId, Integer status) {
        LambdaQueryWrapper<Recharge> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(userId != null, Recharge::getUserId, userId);
        wrapper.eq(status != null, Recharge::getStatus, status);
        wrapper.orderByDesc(Recharge::getCreateTime);
        return page(page, wrapper);
    }
    
    @Override
    public Recharge createRecharge(RechargeRequest request) {
        Recharge recharge = new Recharge();
        recharge.setUserId(request.getUserId());
        recharge.setAmount(request.getAmount());
        recharge.setStatus(0);
        recharge.setPaymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : "alipay");
        recharge.setOrderNo(generateOrderNo());
        recharge.setRemark(request.getRemark());
        save(recharge);
        return recharge;
    }
    
    @Override
    public Recharge getRechargeByOrderNo(String orderNo) {
        LambdaQueryWrapper<Recharge> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Recharge::getOrderNo, orderNo);
        return getOne(wrapper);
    }
    
    @Override
    public boolean updateRechargeStatus(String orderNo, Integer status, String transactionId) {
        LambdaQueryWrapper<Recharge> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Recharge::getOrderNo, orderNo);
        
        Recharge recharge = new Recharge();
        recharge.setStatus(status);
        recharge.setTransactionId(transactionId);
        if (status == 1) {
            recharge.setPayTime(LocalDateTime.now());
        }
        
        return update(recharge, wrapper);
    }
    
    @Override
    public boolean cancelRecharge(Long id) {
        Recharge recharge = getById(id);
        if (recharge == null) {
            return false;
        }
        if (recharge.getStatus() != 0) {
            return false;
        }
        recharge.setStatus(2);
        return updateById(recharge);
    }
    
    private String generateOrderNo() {
        return "RC" + System.currentTimeMillis() + (int)(Math.random() * 10000);
    }
}
