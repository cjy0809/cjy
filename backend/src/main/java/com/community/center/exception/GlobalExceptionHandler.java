package com.community.center.exception;

import com.community.center.common.Result;
import com.community.center.common.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolationException;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.error("业务异常：{}", e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理参数校验异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> {
                    String errorMessage = error.getDefaultMessage();
                    return errorMessage != null ? errorMessage : "参数校验失败";
                })
                .collect(Collectors.joining(", "));
        log.error("参数校验异常：{}", message);
        return Result.error(ResultCode.PARAM_VALIDATION_ERROR.getCode(), message);
    }

    /**
     * 处理参数绑定异常
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleBindException(BindException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> {
                    String errorMessage = error.getDefaultMessage();
                    return errorMessage != null ? errorMessage : "参数绑定失败";
                })
                .collect(Collectors.joining(", "));
        log.error("参数绑定异常：{}", message);
        return Result.error(ResultCode.PARAM_VALIDATION_ERROR.getCode(), message);
    }

    /**
     * 处理路径参数校验异常
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleConstraintViolationException(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .map(violation -> {
                    String errorMessage = violation.getMessage();
                    return errorMessage != null ? errorMessage : "路径参数校验失败";
                })
                .collect(Collectors.joining(", "));
        log.error("路径参数校验异常：{}", message);
        return Result.error(ResultCode.PARAM_VALIDATION_ERROR.getCode(), message);
    }

    /**
     * 处理通用异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.error(ResultCode.INTERNAL_SERVER_ERROR.getCode(), ResultCode.INTERNAL_SERVER_ERROR.getMessage());
    }
}