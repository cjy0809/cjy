package com.community.center.controller;

import com.community.center.common.Result;
import com.community.center.mapper.UserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 系统工具控制器
 */
@Tag(name = "系统工具")
@RestController
@RequestMapping("/api/system")
public class SystemToolController {

    @Autowired
    private UserMapper userMapper;

    @Operation(summary = "更新用户默认头像")
    @PostMapping("/update-default-avatars")
    public Result<Map<String, Object>> updateDefaultAvatars() {
        try {
            // 更新没有头像的用户，设置默认头像
            int updatedCount = userMapper.updateDefaultAvatar();
            
            Map<String, Object> result = new HashMap<>();
            result.put("updatedCount", updatedCount);
            result.put("message", "成功更新了 " + updatedCount + " 个用户的默认头像");
            
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("更新默认头像失败: " + e.getMessage());
        }
    }
}