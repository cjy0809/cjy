package com.community.center.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.community.center.dto.RechargeRequest;
import com.community.center.entity.Recharge;

public interface RechargeService extends IService<Recharge> {
    
    IPage<Recharge> getRechargePage(Page<Recharge> page, Long userId, Integer status);
    
    Recharge createRecharge(RechargeRequest request);
    
    Recharge getRechargeByOrderNo(String orderNo);
    
    boolean updateRechargeStatus(String orderNo, Integer status, String transactionId);
    
    boolean cancelRecharge(Long id);
}
