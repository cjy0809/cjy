package com.community.center.common;

import lombok.Data;

/**
 * 统一响应结果类
 * 用于封装API接口的返回结果，包含状态码、消息和数据
 */
@Data
public class Result<T> {
    private Integer code;
    private String message;
    private T data;

    /**
     * 返回成功结果（无数据）
     * @param <T> 数据类型
     * @return 成功结果
     */
    public static <T> Result<T> success() {
        return success(null);
    }

    /**
     * 返回成功结果（带数据）
     * @param data 返回数据
     * @param <T> 数据类型
     * @return 成功结果
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(ResultCode.SUCCESS.getCode());
        result.setMessage(ResultCode.SUCCESS.getMessage());
        result.setData(data);
        return result;
    }

    /**
     * 返回错误结果（默认错误）
     * @param <T> 数据类型
     * @return 错误结果
     */
    public static <T> Result<T> error() {
        return error(ResultCode.INTERNAL_SERVER_ERROR);
    }

    /**
     * 返回错误结果（自定义消息）
     * @param message 错误消息
     * @param <T> 数据类型
     * @return 错误结果
     */
    public static <T> Result<T> error(String message) {
        return error(ResultCode.INTERNAL_SERVER_ERROR.getCode(), message);
    }

    /**
     * 返回错误结果（使用结果码枚举）
     * @param resultCode 结果码枚举
     * @param <T> 数据类型
     * @return 错误结果
     */
    public static <T> Result<T> error(ResultCode resultCode) {
        return error(resultCode.getCode(), resultCode.getMessage());
    }

    /**
     * 返回错误结果（自定义状态码和消息）
     * @param code 状态码
     * @param message 错误消息
     * @param <T> 数据类型
     * @return 错误结果
     */
    public static <T> Result<T> error(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }
}
