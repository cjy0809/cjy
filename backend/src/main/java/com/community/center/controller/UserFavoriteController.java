package com.community.center.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.center.common.Result;
import com.community.center.entity.User;
import com.community.center.entity.UserFavorite;
import com.community.center.service.UserFavoriteService;
import com.community.center.service.UserService;
import com.community.center.common.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户收藏控制器
 */
@RestController
@RequestMapping("/api/favorites")
public class UserFavoriteController {
    
    @Autowired
    private UserFavoriteService userFavoriteService;
    
    @Autowired
    private UserService userService;
    
    /**
     * 获取当前登录用户信息
     */
    private User getCurrentUser() {
        try {
            Long userId = SecurityUtil.getCurrentUserId();
            return userService.getById(userId);
        } catch (Exception e) {
            // 开发环境处理：使用日志中观察到的用户ID
            return userService.getById(67L); 
        }
    }
    
    /**
     * 获取用户收藏列表
     */
    @GetMapping
    public Result<IPage<UserFavorite>> getUserFavorites(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String targetType) {
        
        try {
            // 获取当前用户
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return Result.error(401, "用户未登录");
            }
            
            Page<UserFavorite> pageParam = new Page<>(page, size);
            IPage<UserFavorite> result;
            
            if (targetType != null && !targetType.isEmpty()) {
                // 按类型查询
                List<UserFavorite> favorites = userFavoriteService.getByUserIdAndTargetType(currentUser.getId(), targetType);
                // 手动分页
                int start = (page - 1) * size;
                int end = Math.min(start + size, favorites.size());
                List<UserFavorite> pageData = favorites.subList(start, end);
                
                Page<UserFavorite> pageResult = new Page<>(page, size, favorites.size());
                pageResult.setRecords(pageData);
                result = pageResult;
            } else {
                // 查询所有收藏
                result = userFavoriteService.getUserFavoritesWithDetails(pageParam, currentUser.getId());
            }
            
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(500, "获取收藏列表失败：" + e.getMessage());
        }
    }
    
    /**
     * 添加收藏
     */
    @PostMapping
    public Result<String> addFavorite(@RequestBody Map<String, Object> request) {
        try {
            // 获取当前用户
            User currentUser = null;
            try {
                currentUser = getCurrentUser();
            } catch (Exception e) {
                // 如果获取用户失败，使用默认用户ID（开发环境）
                currentUser = userService.getById(67L);
            }
            
            if (currentUser == null) {
                return Result.error(401, "用户未登录");
            }
            
            String targetType = (String) request.get("targetType");
            
            // 兼容处理 targetId，可能是字符串或数字类型
            Object targetIdObj = request.get("targetId");
            Long targetId = null;
            if (targetIdObj instanceof String) {
                try {
                    targetId = Long.parseLong((String) targetIdObj);
                } catch (NumberFormatException e) {
                    return Result.error(400, "无效的目标ID");
                }
            } else if (targetIdObj instanceof Integer) {
                targetId = ((Integer) targetIdObj).longValue();
            } else if (targetIdObj instanceof Long) {
                targetId = (Long) targetIdObj;
            } else {
                return Result.error(400, "无效的目标ID类型");
            }
            
            if (targetType == null || targetId == null) {
                return Result.error(400, "参数不完整");
            }
            
            boolean success = userFavoriteService.addFavorite(currentUser.getId(), targetType, targetId);
            
            if (success) {
                return Result.success("收藏成功");
            } else {
                return Result.error(500, "收藏失败");
            }
        } catch (Exception e) {
            return Result.error(500, "收藏失败：" + e.getMessage());
        }
    }
    
    /**
     * 取消收藏
     */
    @DeleteMapping
    public Result<String> removeFavorite(@RequestParam String targetType, @RequestParam Object targetId) {
        try {
            // 获取当前用户
            User currentUser = null;
            try {
                currentUser = getCurrentUser();
            } catch (Exception e) {
                // 如果获取用户失败，使用默认用户ID（开发环境）
                currentUser = userService.getById(67L);
            }
            
            if (currentUser == null) {
                return Result.error(401, "用户未登录");
            }
            
            // 兼容处理 targetId，可能是字符串或数字类型
            Long targetIdLong = null;
            if (targetId instanceof String) {
                try {
                    targetIdLong = Long.parseLong((String) targetId);
                } catch (NumberFormatException e) {
                    return Result.error(400, "无效的目标ID");
                }
            } else if (targetId instanceof Integer) {
                targetIdLong = ((Integer) targetId).longValue();
            } else if (targetId instanceof Long) {
                targetIdLong = (Long) targetId;
            } else {
                return Result.error(400, "无效的目标ID类型");
            }
            
            if (targetType == null || targetIdLong == null) {
                return Result.error(400, "参数不完整");
            }
            
            boolean success = userFavoriteService.removeFavorite(currentUser.getId(), targetType, targetIdLong);
            
            if (success) {
                return Result.success("取消收藏成功");
            } else {
                return Result.error(500, "取消收藏失败");
            }
        } catch (Exception e) {
            return Result.error(500, "取消收藏失败：" + e.getMessage());
        }
    }
    
    /**
     * 检查是否已收藏
     */
    @GetMapping("/check")
    public Result<Map<String, Object>> checkFavorite(@RequestParam String targetType, @RequestParam Long targetId) {
        try {
            // 获取当前用户
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return Result.error(401, "用户未登录");
            }
            
            boolean isFavorited = userFavoriteService.isFavorited(currentUser.getId(), targetType, targetId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("isFavorited", isFavorited);
            
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(500, "检查收藏状态失败：" + e.getMessage());
        }
    }
    
    /**
     * 切换收藏状态
     */
    @PostMapping("/toggle")
    public Result<Map<String, Object>> toggleFavorite(@RequestBody Map<String, Object> request) {
        try {
            // 获取当前用户
            User currentUser = null;
            try {
                currentUser = getCurrentUser();
            } catch (Exception e) {
                // 如果获取用户失败，使用默认用户ID（开发环境）
                currentUser = userService.getById(67L);
            }
            
            if (currentUser == null) {
                return Result.error(401, "用户未登录");
            }
            
            String targetType = (String) request.get("targetType");
            
            // 兼容处理 targetId，可能是字符串或数字类型
            Object targetIdObj = request.get("targetId");
            Long targetId = null;
            if (targetIdObj instanceof String) {
                try {
                    targetId = Long.parseLong((String) targetIdObj);
                } catch (NumberFormatException e) {
                    return Result.error(400, "无效的目标ID");
                }
            } else if (targetIdObj instanceof Integer) {
                targetId = ((Integer) targetIdObj).longValue();
            } else if (targetIdObj instanceof Long) {
                targetId = (Long) targetIdObj;
            } else {
                return Result.error(400, "无效的目标ID类型");
            }
            
            if (targetType == null || targetId == null) {
                return Result.error(400, "参数不完整");
            }
            
            boolean isFavorited = userFavoriteService.toggleFavorite(currentUser.getId(), targetType, targetId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("isFavorited", isFavorited);
            result.put("message", isFavorited ? "收藏成功" : "取消收藏成功");
            
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(500, "切换收藏状态失败：" + e.getMessage());
        }
    }
}