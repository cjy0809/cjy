package com.community.center.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.community.center.common.PageResult;
import com.community.center.common.Result;
import com.community.center.dto.PageQueryDTO;
import com.community.center.entity.Payment;
import com.community.center.entity.ServiceReservation;
import com.community.center.entity.User;
import com.community.center.service.AlipayService;
import com.community.center.service.PaymentService;
import com.community.center.service.ServiceReservationService;
import com.community.center.service.UserService;
import com.community.center.common.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {
    
    @Autowired
    private PaymentService paymentService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private AlipayService alipayService;
    
    @Autowired
    private ServiceReservationService serviceReservationService;
    
    @Autowired
    private com.community.center.service.RechargeService rechargeService;
    
    @PostMapping("/balance/pay")
    public Result<Boolean> balancePay(@RequestBody Payment payment, HttpServletRequest request) {
        System.out.println("=== 余额支付开始 ===");
        
        try {
            Long userId = null;
            try {
                userId = SecurityUtil.getCurrentUserId();
            } catch (Exception e) {
                userId = (Long) request.getAttribute("userId");
            }
            
            if (userId == null) {
                System.out.println("余额支付失败：无法获取用户ID");
                return Result.error(401, "无法获取用户ID");
            }
            
            System.out.println("余额支付用户ID：" + userId);
            System.out.println("余额支付预约ID：" + payment.getBookingId());
            System.out.println("余额支付服务ID：" + payment.getServiceId());
            System.out.println("余额支付金额：" + payment.getAmount());
            
            // 验证用户是否存在
            User user = userService.getById(userId);
            if (user == null) {
                System.out.println("余额支付失败：用户不存在");
                return Result.error(404, "用户不存在");
            }
            
            // 验证余额是否充足
            if (user.getBalance().compareTo(payment.getAmount()) < 0) {
                System.out.println("余额支付失败：余额不足");
                System.out.println("当前余额：" + user.getBalance());
                System.out.println("需要金额：" + payment.getAmount());
                return Result.error(400, "账户余额不足");
            }
            
            // 验证预约是否存在
            ServiceReservation reservation = serviceReservationService.getById(payment.getBookingId());
            if (reservation == null) {
                System.out.println("余额支付失败：预约不存在");
                return Result.error(404, "预约不存在");
            }
            
            // 检查是否已经支付
            Payment existingPayment = paymentService.getPaymentByBookingId(payment.getBookingId());
            if (existingPayment != null && existingPayment.getStatus() == 1) {
                System.out.println("余额支付失败：已经支付过了");
                return Result.error(400, "该订单已经支付过了");
            }
            
            // 开始余额支付
            payment.setUserId(userId);
            payment.setStatus(0);
            payment.setPaymentMethod("1"); // "1": 余额支付
            
            // 保存支付记录
            Payment savedPayment = paymentService.createPayment(payment);
            if (savedPayment == null) {
                System.out.println("余额支付失败：创建支付记录失败");
                return Result.error(500, "创建支付记录失败");
            }
            
            System.out.println("余额支付记录创建成功，ID：" + savedPayment.getId());
            
            // 扣除用户余额
            user.setBalance(user.getBalance().subtract(payment.getAmount()));
            boolean balanceUpdated = userService.updateById(user);
            if (!balanceUpdated) {
                System.out.println("余额支付失败：更新余额失败");
                // 回滚支付状态
                paymentService.updatePaymentStatus(savedPayment.getId(), 2); // 2: 支付失败
                return Result.error(500, "更新余额失败");
            }
            
            System.out.println("余额扣除成功，更新后余额：" + user.getBalance());
            
            // 更新支付状态为成功
            boolean paymentUpdated = paymentService.updatePaymentStatus(savedPayment.getId(), 1); // 1: 已支付
            if (!paymentUpdated) {
                System.out.println("余额支付失败：更新支付状态失败");
                // 回滚余额
                user.setBalance(user.getBalance().add(payment.getAmount()));
                userService.updateById(user);
                return Result.error(500, "更新支付状态失败");
            }
            
            // 更新预约状态为已支付
            reservation.setStatus(2); // 2: 已支付
            boolean reservationUpdated = serviceReservationService.updateById(reservation);
            if (!reservationUpdated) {
                System.out.println("余额支付失败：更新预约状态失败");
                return Result.error(500, "更新预约状态失败");
            }
            
            System.out.println("余额支付成功！");
            System.out.println("=== 余额支付结束 ===");
            return Result.success(true);
            
        } catch (Exception e) {
            System.out.println("余额支付异常：" + e.getMessage());
            e.printStackTrace();
            return Result.error(500, "支付失败：" + e.getMessage());
        }
    }
    
    private User getCurrentUser() {
        Long userId = SecurityUtil.getCurrentUserId();
        return userService.getById(userId);
    }
    
    @GetMapping("/page")
    public Result<PageResult<Payment>> getPaymentPage(PageQueryDTO query) {
        IPage<Payment> page = paymentService.getPaymentPage(
                query.getPageNum(), 
                query.getPageSize(), 
                query.getUserId(), 
                query.getServiceId(), 
                query.getStatus());
        PageResult<Payment> pageResult = new PageResult<>(
                200,
                "success",
                page.getTotal(),
                page.getRecords()
        );
        return Result.success(pageResult);
    }
    
    @GetMapping
    public List<Payment> getAllPayments() {
        IPage<Payment> page = paymentService.getPaymentPage(1, 100, null, null, null);
        return page.getRecords();
    }
    
    @GetMapping("/user/{userId}")
    public List<Payment> getPaymentsByUserId(@PathVariable Long userId) {
        return paymentService.getPaymentsByUserId(userId);
    }
    
    @GetMapping("/booking/{bookingId}")
    public Result<Payment> getPaymentByBookingId(@PathVariable Long bookingId) {
        try {
            Payment payment = paymentService.getPaymentByBookingId(bookingId);
            
            if (payment != null) {
                return Result.success(payment);
            }
            return Result.error(404, "Payment not found");
        } catch (Exception e) {
            throw e;
        }
    }
    
    @GetMapping("/{id}/status")
    public Result<Integer> getPaymentStatus(@PathVariable Long id) {
        Payment payment = paymentService.getById(id);
        if (payment != null) {
            return Result.success(payment.getStatus());
        }
        return Result.error(404, "Payment not found");
    }
    
    @GetMapping("/history")
    public Result<PageResult<Payment>> getPaymentHistory(PageQueryDTO query) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return Result.error(401, "Unauthorized");
        }
        
        IPage<Payment> page = paymentService.getPaymentPage(
                query.getPageNum(), 
                query.getPageSize(), 
                currentUser.getId(), 
                null, 
                null);
        PageResult<Payment> pageResult = new PageResult<>(
                200,
                "success",
                page.getTotal(),
                page.getRecords()
        );
        return Result.success(pageResult);
    }
    
    @PostMapping
    public Result<Payment> createPayment(@RequestBody Payment payment) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return Result.error(401, "Unauthorized");
        }
        
        payment.setUserId(currentUser.getId());
        Payment createdPayment = paymentService.createPayment(payment);
        
        if (createdPayment != null) {
            return Result.success(createdPayment);
        }
        return Result.error(500, "Failed to create payment");
    }
    
    @PostMapping("/cancel")
    public Result<Boolean> cancelPayment(@RequestBody Payment payment) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return Result.error(401, "Unauthorized");
        }
        
        boolean success = paymentService.cancelPayment(payment.getBookingId());
        
        if (success) {
            return Result.success(true);
        }
        return Result.error(500, "Failed to cancel payment");
    }
    
    @DeleteMapping("/{id}")
    public Result<Boolean> deletePayment(@PathVariable Long id) {
        boolean success = paymentService.deletePayment(id);
        
        if (success) {
            return Result.success(true);
        }
        return Result.error(500, "Failed to delete payment");
    }
    
    @PutMapping("/{id}/status")
    public Result<Boolean> updatePaymentStatus(@PathVariable Long id, @RequestParam Integer status) {
        boolean success = paymentService.updatePaymentStatus(id, status);
        
        if (success) {
            return Result.success(true);
        }
        return Result.error(500, "Failed to update payment status");
    }
    
    @PostMapping("/alipay/create")
    public void createAlipayPayment(@RequestBody Payment payment, HttpServletResponse response, HttpServletRequest request) throws IOException {
        System.out.println("创建支付宝支付请求: " + payment);
        
        Long userId = null;
        
        try {
            userId = SecurityUtil.getCurrentUserId();
        } catch (Exception e) {
            userId = (Long) request.getAttribute("userId");
        }
        
        if (userId == null) {
            response.setContentType("application/json;charset=UTF-8");
            PrintWriter writer = response.getWriter();
            writer.write("{\"code\":401,\"message\":\"无法从认证信息中获取用户ID\"}");
            writer.flush();
            return;
        }
        
        payment.setUserId(userId);
        payment.setStatus(0);
        
        Payment savedPayment = paymentService.createPayment(payment);
        if (savedPayment == null) {
            response.setContentType("application/json;charset=UTF-8");
            PrintWriter writer = response.getWriter();
            writer.write("{\"code\":500,\"message\":\"保存支付记录失败\"}");
            writer.flush();
            return;
        }
        
        String outTradeNo = "PAY" + System.currentTimeMillis() + "_" + payment.getBookingId();
        String subject = "社区服务预约支付";
        String body = "服务ID: " + payment.getServiceId();
        
        try {
            String payForm = alipayService.createPayment(outTradeNo, payment.getAmount(), subject, body);
            response.setContentType("text/html;charset=UTF-8");
            PrintWriter writer = response.getWriter();
            writer.write(payForm);
            writer.flush();
        } catch (Exception e) {
            response.setContentType("application/json;charset=UTF-8");
            PrintWriter writer = response.getWriter();
            writer.write("{\"code\":500,\"message\":\"创建支付失败: " + e.getMessage() + "\"}");
            writer.flush();
        }
    }
    
    @PostMapping("/alipay/notify")
    public void alipayNotify(@RequestParam Map<String, String> params, HttpServletResponse response) throws IOException {
        boolean verifyResult = alipayService.verifyNotify(params);
        if (!verifyResult) {
            response.setContentType("text/plain;charset=UTF-8");
            PrintWriter writer = response.getWriter();
            writer.write("fail");
            writer.flush();
            return;
        }
        
        String outTradeNo = params.get("out_trade_no");
        String tradeStatus = params.get("trade_status");
        String tradeNo = params.get("trade_no");
        
        if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
            try {
                if (outTradeNo == null) {
                    response.setContentType("text/plain;charset=UTF-8");
                    PrintWriter writer = response.getWriter();
                    writer.write("fail");
                    writer.flush();
                    return;
                }
                
                if (outTradeNo.startsWith("RC")) {
                    com.community.center.entity.Recharge recharge = rechargeService.getRechargeByOrderNo(outTradeNo);
                    if (recharge != null && recharge.getStatus() == 0) {
                        rechargeService.updateRechargeStatus(outTradeNo, 1, tradeNo);
                        
                        User user = userService.getById(recharge.getUserId());
                        if (user != null) {
                            user.setBalance(user.getBalance().add(recharge.getAmount()));
                            userService.updateById(user);
                        }
                    }
                } else if (outTradeNo.startsWith("PAY") && outTradeNo.contains("_")) {
                    String bookingIdStr = outTradeNo.substring(outTradeNo.lastIndexOf("_") + 1);
                    
                    Long bookingId = null;
                    try {
                        bookingId = Long.parseLong(bookingIdStr);
                    } catch (NumberFormatException e) {
                        response.setContentType("text/plain;charset=UTF-8");
                        PrintWriter writer = response.getWriter();
                        writer.write("fail");
                        writer.flush();
                        return;
                    }
                    
                    Payment payment = paymentService.getPaymentByBookingId(bookingId);
                    if (payment != null) {
                        payment.setStatus(1);
                        payment.setTransactionId(tradeNo);
                        paymentService.updateById(payment);
                        
                        ServiceReservation reservation = serviceReservationService.getById(bookingId);
                        if (reservation != null && reservation.getStatus() == 0) {
                            reservation.setStatus(1);
                            serviceReservationService.updateById(reservation);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                response.setContentType("text/plain;charset=UTF-8");
                PrintWriter writer = response.getWriter();
                writer.write("fail");
                writer.flush();
                return;
            }
        }
        
        response.setContentType("text/plain;charset=UTF-8");
        PrintWriter writer = response.getWriter();
        writer.write("success");
        writer.flush();
    }
    
    @GetMapping("/alipay/return")
    public Result<String> alipayReturn(@RequestParam Map<String, String> params) {
        boolean verifyResult = alipayService.verifyNotify(params);
        if (!verifyResult) {
            return Result.error(400, "签名验证失败");
        }
        
        String tradeStatus = params.get("trade_status");
        
        if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
            return Result.success("支付成功");
        } else {
            return Result.error(400, "支付未完成");
        }
    }
}
