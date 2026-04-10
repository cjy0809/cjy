package com.community.center.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.center.common.Result;
import com.community.center.common.SecurityUtil;
import com.community.center.dto.RechargeRequest;
import com.community.center.entity.Recharge;
import com.community.center.entity.User;
import com.community.center.service.AlipayService;
import com.community.center.service.RechargeService;
import com.community.center.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/recharge")
public class RechargeController {
    
    @Resource
    private RechargeService rechargeService;
    
    @Resource
    private UserService userService;
    
    @Resource
    private AlipayService alipayService;
    
    @GetMapping("/page")
    public Result<IPage<Recharge>> getRechargePage(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Integer status) {
        Page<Recharge> pageParam = new Page<>(page, size);
        IPage<Recharge> result = rechargeService.getRechargePage(pageParam, userId, status);
        return Result.success(result);
    }
    
    @PostMapping("/create")
    public Result<Map<String, Object>> createRecharge(@RequestBody RechargeRequest request) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        request.setUserId(currentUserId);
        
        Recharge recharge = rechargeService.createRecharge(request);
        
        String payUrl = alipayService.createPayment(
            recharge.getOrderNo(),
            recharge.getAmount(),
            "账户充值",
            "用户自主充值",
            "http://localhost:3000/elderly/profile"
        );
        
        Map<String, Object> result = new HashMap<>();
        result.put("recharge", recharge);
        result.put("payUrl", payUrl);
        
        return Result.success(result);
    }
    
    @GetMapping("/order/{orderNo}")
    public Result<Recharge> getRechargeByOrderNo(@PathVariable String orderNo) {
        Recharge recharge = rechargeService.getRechargeByOrderNo(orderNo);
        return Result.success(recharge);
    }
    
    @GetMapping("/{id}")
    public Result<Recharge> getRechargeById(@PathVariable Long id) {
        Recharge recharge = rechargeService.getById(id);
        return Result.success(recharge);
    }
    
    @PutMapping("/status/{orderNo}")
    public Result<Boolean> updateRechargeStatus(
            @PathVariable String orderNo,
            @RequestParam Integer status,
            @RequestParam(required = false) String transactionId) {
        
        if (status == 1) {
            Recharge recharge = rechargeService.getRechargeByOrderNo(orderNo);
            if (recharge != null && recharge.getStatus() == 0) {
                User user = userService.getById(recharge.getUserId());
                if (user != null) {
                    user.setBalance(user.getBalance().add(recharge.getAmount()));
                    userService.updateById(user);
                }
            }
        }
        
        boolean success = rechargeService.updateRechargeStatus(orderNo, status, transactionId);
        return Result.success(success);
    }
    
    @PutMapping("/cancel/{id}")
    public Result<Boolean> cancelRecharge(@PathVariable Long id) {
        boolean success = rechargeService.cancelRecharge(id);
        return Result.success(success);
    }
    
    @GetMapping("/pay/{id}")
    public Result<Map<String, Object>> getPayUrl(@PathVariable Long id) {
        System.out.println("=== 开始处理支付请求，订单ID: " + id);
        Recharge recharge = rechargeService.getById(id);
        if (recharge == null) {
            System.out.println("=== 订单不存在");
            return Result.error("充值订单不存在");
        }
        System.out.println("=== 订单状态: " + recharge.getStatus());
        if (recharge.getStatus() != 0) {
            System.out.println("=== 订单状态不正确，无法支付");
            return Result.error("订单状态不正确，无法支付");
        }
        
        System.out.println("=== 开始生成支付URL，订单号: " + recharge.getOrderNo() + ", 金额: " + recharge.getAmount());
        String payUrl = alipayService.createPayment(
            recharge.getOrderNo(),
            recharge.getAmount(),
            "账户充值",
            "用户自主充值",
            "http://localhost:3000/elderly/recharge"
        );
        
        System.out.println("=== 支付URL生成成功: " + payUrl);
        
        Map<String, Object> result = new HashMap<>();
        result.put("payUrl", payUrl);
        result.put("orderNo", recharge.getOrderNo());
        result.put("formHtml", payUrl);
        
        return Result.success(result);
    }
}
