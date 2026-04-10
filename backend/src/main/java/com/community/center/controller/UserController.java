package com.community.center.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.center.common.PageResult;
import com.community.center.common.Result;
import com.community.center.dto.request.UserLoginRequest;
import com.community.center.dto.request.UserRegisterRequest;
import com.community.center.dto.request.UserUpdateRequest;
import com.community.center.dto.response.UserInfoResponse;
import com.community.center.dto.response.UserLoginResponse;
import com.community.center.entity.User;
import com.community.center.service.UserService;
import com.community.center.common.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Map;

import javax.validation.Valid;

/**
 * 用户控制器
 * 处理用户相关的所有请求，包括登录、注册、信息更新、密码修改等
 */
@Tag(name = "用户管理")
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<UserLoginResponse> login(@Valid @RequestBody UserLoginRequest request) {
        UserLoginResponse response = userService.login(request);
        return Result.success(response);
    }

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> register(@Valid @RequestBody UserRegisterRequest request) {
        userService.register(request);
        return Result.success();
    }

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/info")
    public Result<UserInfoResponse> getCurrentUser() {
        UserInfoResponse response = userService.getCurrentUser();
        return Result.success(response);
    }

    @Operation(summary = "更新当前用户信息")
    @PutMapping("/info")
    public Result<Void> updateCurrentUser(@Valid @RequestBody UserUpdateRequest request) {
        userService.updateUser(request);
        return Result.success();
    }
    
    @Operation(summary = "上传用户头像")
    @PostMapping("/avatar")
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        try {
            System.out.println("UserController - 接收到头像上传请求，文件名: " + file.getOriginalFilename());
            String avatarUrl = userService.uploadAvatar(file);
            System.out.println("UserController - 头像上传成功，URL: " + avatarUrl);
            return Result.success(avatarUrl);
        } catch (Exception e) {
            System.out.println("UserController - 头像上传失败: " + e.getMessage());
            e.printStackTrace();
            return Result.error("头像上传失败：" + e.getMessage());
        }
    }
    
    @Operation(summary = "更新指定用户信息")
    @PutMapping("/{userId}/info")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('STAFF') and @userService.isUserElderly(#userId))")
    public Result<Void> updateUserInfo(
            @Parameter(description = "用户ID", required = true) @PathVariable Long userId,
            @Valid @RequestBody UserUpdateRequest request) {
        try {
            // 工作人员不能修改用户角色
            if (!SecurityUtil.isAdmin() && request.getRole() != null) {
                return Result.error("工作人员无权修改用户角色");
            }
            
            // 只有系统管理员（13800138000）才能修改其他管理员的信息
            if (SecurityUtil.isAdmin() && !SecurityUtil.isSystemAdmin()) {
                User targetUser = userService.getById(userId);
                if (targetUser != null && "ADMIN".equals(targetUser.getRole())) {
                    return Result.error("只有系统管理员才能修改其他管理员的信息");
                }
            }
            
            userService.updateUserById(userId, request);
            return Result.success();
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "修改密码")
    @PutMapping("/password")
    public Result<Void> changePassword(
            @Parameter(description = "旧密码", required = true) @RequestParam String oldPassword,
            @Parameter(description = "新密码", required = true) @RequestParam String newPassword) {
        userService.changePassword(oldPassword, newPassword);
        return Result.success();
    }

    @Operation(summary = "分页查询用户")
    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public Result<PageResult<UserInfoResponse>> getUserPage(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小", example = "10") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "用户名") @RequestParam(required = false) String username,
            @Parameter(description = "姓名") @RequestParam(required = false) String name,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status,
            @Parameter(description = "角色") @RequestParam(required = false) String role,
            @Parameter(description = "开始时间") @RequestParam(required = false) String startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) String endTime) {
        // 转换时间字符串为LocalDateTime对象
        LocalDateTime startDateTime = null;
        LocalDateTime endDateTime = null;
        if (StringUtils.hasText(startTime)) {
            startDateTime = LocalDateTime.parse(startTime);
        }
        if (StringUtils.hasText(endTime)) {
            endDateTime = LocalDateTime.parse(endTime);
        }
        
        Page<UserInfoResponse> userPage = userService.getUserPage(page, size, username, name, status, role, startDateTime, endDateTime);
        PageResult<UserInfoResponse> pageResult = PageResult.success(userPage);
        return Result.success(pageResult);
    }

    @Operation(summary = "更新用户状态")
    @PutMapping("/{userId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> updateUserStatus(
            @Parameter(description = "用户ID", required = true) @PathVariable Long userId,
            @Parameter(description = "状态", required = true) @RequestParam Integer status) {
        // 只有系统管理员（13800138000）才能修改其他管理员的状态
        if (!SecurityUtil.isSystemAdmin()) {
            User targetUser = userService.getById(userId);
            if (targetUser != null && "ADMIN".equals(targetUser.getRole())) {
                return Result.error("只有系统管理员才能修改其他管理员的状态");
            }
        }
        
        userService.updateUserStatus(userId, status);
        return Result.success();
    }
    
    @Operation(summary = "删除用户")
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteUser(
            @Parameter(description = "用户ID", required = true) @PathVariable Long userId) {
        try {
            // 只有系统管理员（13800138000）才能删除其他管理员
            if (!SecurityUtil.isSystemAdmin()) {
                User targetUser = userService.getById(userId);
                if (targetUser != null && "ADMIN".equals(targetUser.getRole())) {
                    return Result.error("只有系统管理员才能删除其他管理员");
                }
            }
            
            userService.deleteUserWithCascade(userId);
            return Result.success();
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }
    
    @Operation(summary = "获取用户关联数据统计")
    @GetMapping("/{userId}/related-data-count")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Map<String, Integer>> getUserRelatedDataCount(
            @Parameter(description = "用户ID", required = true) @PathVariable Long userId) {
        try {
            Map<String, Integer> countMap = userService.getUserRelatedDataCount(userId);
            return Result.success(countMap);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }
    
    @Operation(summary = "根据ID获取用户信息")
    @GetMapping("/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public Result<UserInfoResponse> getUserById(
            @Parameter(description = "用户ID", required = true) @PathVariable Long userId) {
        try {
            UserInfoResponse user = userService.getUserById(userId);
            if (user != null) {
                return Result.success(user);
            } else {
                return Result.error("用户不存在");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }
}
