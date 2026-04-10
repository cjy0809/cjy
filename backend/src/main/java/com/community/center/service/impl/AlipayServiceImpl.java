package com.community.center.service.impl;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.community.center.service.AlipayService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

@Service
public class AlipayServiceImpl implements AlipayService {
    
    @Value("${alipay.gateway-url:https://openapi.alipaydev.com/gateway.do}")
    private String gatewayUrl;
    
    @Value("${alipay.app-id:2021000000000000}")
    private String appId;
    
    @Value("${alipay.private-key:MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggieAgEAAoIBAQC}")
    private String privateKey;
    
    @Value("${alipay.alipay-public-key:MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA}")
    private String alipayPublicKey;
    
    @Value("${alipay.charset:UTF-8}")
    private String charset;
    
    @Value("${alipay.sign-type:RSA2}")
    private String signType;
    
    @Value("${alipay.format:json}")
    private String format;
    
    @Value("${alipay.notify-url:http://localhost:8080/api/alipay/notify}")
    private String notifyUrl;
    
    @Value("${alipay.return-url:http://localhost:3000/elderly/profile}")
    private String returnUrl;
    
    @Override
    public String createPayOrder(String orderNo, BigDecimal amount, String subject) {
        AlipayClient alipayClient = new DefaultAlipayClient(
            gatewayUrl,
            appId,
            privateKey,
            format,
            charset,
            alipayPublicKey,
            signType
        );
        
        AlipayTradePrecreateRequest request = new AlipayTradePrecreateRequest();
        request.setBizContent("{" +
            "\"out_trade_no\":\"" + orderNo + "\"," +
            "\"total_amount\":\"" + amount + "\"," +
            "\"subject\":\"" + subject + "\"," +
            "\"timeout_express\":\"30m\"}");
        
        request.setNotifyUrl(notifyUrl);
        request.setReturnUrl(returnUrl);
        
        try {
            AlipayTradePrecreateResponse response = alipayClient.execute(request);
            
            if (response.isSuccess()) {
                return response.getQrCode();
            } else {
                throw new RuntimeException("创建支付宝订单失败: " + response.getSubMsg());
            }
        } catch (AlipayApiException e) {
            throw new RuntimeException("支付宝API调用失败", e);
        }
    }
    
    @Override
    public boolean verifyPayment(String orderNo, String tradeNo) {
        return true;
    }
    
    @Override
    public String createPayment(String outTradeNo, BigDecimal amount, String subject, String body) {
        return createPayment(outTradeNo, amount, subject, body, returnUrl);
    }
    
    @Override
    public String createPayment(String outTradeNo, BigDecimal amount, String subject, String body, String returnUrl) {
        AlipayClient alipayClient = new DefaultAlipayClient(
            gatewayUrl,
            appId,
            privateKey,
            format,
            charset,
            alipayPublicKey,
            signType
        );
        
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setBizContent("{" +
            "\"out_trade_no\":\"" + outTradeNo + "\"," +
            "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"," +
            "\"total_amount\":\"" + amount + "\"," +
            "\"subject\":\"" + subject + "\"," +
            "\"body\":\"" + body + "\"," +
            "\"timeout_express\":\"30m\"}");
        
        request.setNotifyUrl(notifyUrl);
        request.setReturnUrl(returnUrl);
        
        try {
            AlipayTradePagePayResponse response = alipayClient.pageExecute(request);
            
            if (response.isSuccess()) {
                return gatewayUrl + "?" + response.getBody();
            } else {
                throw new RuntimeException("创建支付宝支付失败: " + response.getSubMsg());
            }
        } catch (AlipayApiException e) {
            throw new RuntimeException("支付宝API调用失败", e);
        }
    }
    
    @Override
    public boolean verifyNotify(Map<String, String> params) {
        return true;
    }
}
