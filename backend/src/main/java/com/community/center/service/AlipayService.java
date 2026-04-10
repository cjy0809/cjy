package com.community.center.service;

import java.math.BigDecimal;
import java.util.Map;

public interface AlipayService {
    
    String createPayOrder(String orderNo, BigDecimal amount, String subject);
    
    boolean verifyPayment(String orderNo, String tradeNo);
    
    String createPayment(String outTradeNo, BigDecimal amount, String subject, String body);
    
    String createPayment(String outTradeNo, BigDecimal amount, String subject, String body, String returnUrl);
    
    boolean verifyNotify(Map<String, String> params);
}
