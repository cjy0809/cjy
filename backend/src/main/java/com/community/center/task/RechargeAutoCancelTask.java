package com.community.center.task;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.community.center.entity.Recharge;
import com.community.center.mapper.RechargeMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class RechargeAutoCancelTask {

    private static final Logger logger = LoggerFactory.getLogger(RechargeAutoCancelTask.class);
    
    private static final int TIMEOUT_MINUTES = 5;
    
    @Autowired
    private RechargeMapper rechargeMapper;

    @Scheduled(fixedRate = 60000)
    public void autoCancelExpiredRecharges() {
        try {
            logger.info("开始执行超时充值记录自动取消任务");
            
            LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(TIMEOUT_MINUTES);
            
            QueryWrapper<Recharge> rechargeWrapper = new QueryWrapper<>();
            rechargeWrapper.eq("status", 0);
            rechargeWrapper.lt("create_time", fiveMinutesAgo);
            
            List<Recharge> expiredRecharges = rechargeMapper.selectList(rechargeWrapper);
            
            if (!expiredRecharges.isEmpty()) {
                for (Recharge recharge : expiredRecharges) {
                    logger.info("自动取消超时充值记录: ID={}, 订单号={}, 用户ID={}, 创建时间={}", 
                        recharge.getId(), recharge.getOrderNo(), recharge.getUserId(), recharge.getCreateTime());
                    
                    recharge.setStatus(2);
                    rechargeMapper.updateById(recharge);
                    
                    logger.info("已取消充值记录: 充值ID={}, 订单号={}, 状态更新为已取消", recharge.getId(), recharge.getOrderNo());
                }
                
                logger.info("超时充值记录自动取消任务完成，共取消{}条记录", expiredRecharges.size());
            } else {
                logger.info("没有需要自动取消的超时充值记录");
            }
        } catch (Exception e) {
            logger.error("执行超时充值记录自动取消任务时发生错误", e);
        }
    }
}