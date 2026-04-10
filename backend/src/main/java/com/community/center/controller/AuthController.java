package com.community.center.controller;

import com.community.center.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证控制器
 * 处理用户认证相关的请求，包括登出、刷新令牌等
 */
@Tag(name = "认证管理")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Operation(summary = "用户登出")
    @PostMapping("/logout")
    public Result<Void> logout() {
        // 登出逻辑：实际项目中可能需要清除token或在Redis中标记token失效
        // 这里前端会自行清除token，后端返回成功即可
        return Result.success();
    }

    @Operation(summary = "刷新令牌")
    @PostMapping("/refresh")
    public Result<Void> refreshToken() {
        // 刷新令牌逻辑：实际项目中需要验证旧token并生成新token
        // 暂时返回成功，后续可根据实际需求实现完整逻辑
        return Result.success();
    }
}
