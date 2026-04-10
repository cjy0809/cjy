package com.community.center.common;

import lombok.Getter;

/**
 * 响应状态码枚举
 */
@Getter
public enum ResultCode {
    SUCCESS(200, "操作成功"),
    BAD_REQUEST(400, "请求参数错误"),
    PARAM_VALIDATION_ERROR(400, "参数验证失败"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "禁止访问"),
    NO_PERMISSION(403, "没有操作权限"),
    NOT_FOUND(404, "未找到资源"),
    INTERNAL_SERVER_ERROR(500, "服务器内部错误"),
    
    // 业务状态码
    USER_NOT_FOUND(1001, "用户不存在"),
    USER_ALREADY_EXISTS(1002, "用户已存在"),
    PASSWORD_ERROR(1003, "密码错误"),
    USER_DISABLED(1004, "用户已禁用"),
    USER_NOT_REVIEWED(1005, "用户未审核"),
    PHONE_ALREADY_EXISTS(1006, "手机号已存在"),
    USER_REGISTER_FAILED(1007, "用户注册失败"),
    
    ACTIVITY_NOT_FOUND(2001, "活动不存在"),
    ACTIVITY_FULL(2002, "活动报名已满"),
    ACTIVITY_NOT_REGISTRABLE(2003, "活动不可报名"),
    ALREADY_REGISTERED(2004, "已报名该活动"),
    NOT_REGISTERED(2005, "未报名该活动"),
    REGISTRATION_NOT_FOUND(2006, "报名记录不存在"),
    
    VENUE_NOT_FOUND(3001, "场地不存在"),
    VENUE_NOT_AVAILABLE(3002, "场地不可用"),
    RESERVATION_CONFLICT(3003, "预约时间冲突"),
    RESERVATION_NOT_FOUND(3004, "预约记录不存在"),
    
    HEALTH_RECORD_NOT_FOUND(4001, "健康记录不存在"),
    
    NEWS_NOT_FOUND(5001, "新闻不存在"),
    NEWS_CREATE_FAILED(5002, "新闻创建失败"),
    NEWS_UPDATE_FAILED(5003, "新闻更新失败"),
    NEWS_DELETE_FAILED(5004, "新闻删除失败"),
    
    ;
    
    private final Integer code;
    private final String message;
    
    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}