package com.community.center.common;

import com.community.center.entity.User;
import com.community.center.exception.BusinessException;
import com.community.center.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * 安全上下文工具类
 * 提供获取当前登录用户信息、角色判断等安全相关功能
 */
@Component
public class SecurityUtil {

    /**
     * 获取当前认证用户ID
     * @return 当前用户ID
     * @throws BusinessException 未认证或获取失败时抛出异常
     */
    public static Long getCurrentUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            System.out.println("SecurityUtil - 获取认证对象: " + authentication);
            
            if (authentication == null || !authentication.isAuthenticated()) {
                System.out.println("SecurityUtil - 认证对象为空或未认证");
                throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), ResultCode.UNAUTHORIZED.getMessage());
            }
            
            // 从认证信息中获取用户ID
            Object principal = authentication.getPrincipal();
            System.out.println("SecurityUtil - 获取Principal: " + principal);
            
            // 开发环境处理：如果principal是字符串
            if (principal instanceof String) {
                String principalStr = (String) principal;
                System.out.println("SecurityUtil - Principal是字符串: " + principalStr);
                
                // 检查是否是有效的 JWT 令牌（JWT 必须包含 2 个点）
                if (principalStr.contains(".") && principalStr.chars().filter(ch -> ch == '.').count() == 2) {
                    try {
                        // 尝试作为JWT令牌解析
                        JwtUtil jwtUtil = SpringContextHolder.getBean(JwtUtil.class);
                        Long userId = jwtUtil.getUserIdFromToken(principalStr);
                        System.out.println("SecurityUtil - 从JWT解析用户ID: " + userId);
                        if (userId != null) {
                            return userId;
                        }
                    } catch (Exception e) {
                        System.out.println("SecurityUtil - JWT解析失败: " + e.getMessage());
                    }
                }
                
                // JWT解析失败或不是有效的JWT，从请求属性中获取userId
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attributes != null) {
                    HttpServletRequest request = attributes.getRequest();
                    if (request != null) {
                        Object userIdObj = request.getAttribute("userId");
                        System.out.println("SecurityUtil - 从请求属性获取用户ID: " + userIdObj);
                        if (userIdObj instanceof Long) {
                            return (Long) userIdObj;
                        } else if (userIdObj instanceof String) {
                            return Long.valueOf((String) userIdObj);
                        }
                    }
                }
                
                // 如果请求属性中也没有，尝试直接从authentication.getName()获取
                try {
                    Long userId = Long.valueOf(authentication.getName());
                    System.out.println("SecurityUtil - 从getName获取用户ID: " + userId);
                    return userId;
                } catch (NumberFormatException ex) {
                    System.out.println("SecurityUtil - getName无法转换为Long: " + authentication.getName());
                }
            }
            
            // 其他情况，从自定义的UserDetails中获取
            try {
                // 尝试直接从authentication.getName()获取
                Long userId = Long.valueOf(authentication.getName());
                System.out.println("SecurityUtil - 最终从getName获取用户ID: " + userId);
                return userId;
            } catch (Exception e) {
                System.out.println("SecurityUtil - 所有方法都失败: " + e.getMessage());
                throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "无法从认证信息中获取用户ID");
            }
        } catch (BusinessException e) {
            System.out.println("SecurityUtil - BusinessException: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.out.println("SecurityUtil - 其他异常: " + e.getMessage());
            throw new BusinessException(ResultCode.INTERNAL_SERVER_ERROR.getCode(), "获取用户ID失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取当前认证用户名
     * @return 当前用户名
     * @throws BusinessException 未认证或获取失败时抛出异常
     */
    public static String getCurrentUsername() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), ResultCode.UNAUTHORIZED.getMessage());
            }
            
            return authentication.getName();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(ResultCode.INTERNAL_SERVER_ERROR.getCode(), "获取用户名失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取当前认证用户角色
     * @return 当前用户角色（ADMIN/STAFF/ELDERLY）
     * @throws BusinessException 未认证或获取失败时抛出异常
     */
    public static String getCurrentUserRole() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), ResultCode.UNAUTHORIZED.getMessage());
            }
            
            // 从认证信息中获取用户角色
            String role;
            Object principal = authentication.getPrincipal();
            if (principal instanceof String) {
                String principalStr = (String) principal;
                
                // 检查是否是有效的 JWT 令牌（JWT 必须包含 2 个点）
                if (principalStr.contains(".") && principalStr.chars().filter(ch -> ch == '.').count() == 2) {
                    // 如果是JWT字符串，需要解析
                    JwtUtil jwtUtil = SpringContextHolder.getBean(JwtUtil.class);
                    String rawRole = jwtUtil.getRoleFromToken(principalStr);
                    
                    // 角色映射：将数字角色转换为字符串角色
                    switch (rawRole) {
                        case "1": role = "STAFF"; break;
                        case "2": role = "ELDERLY"; break;
                        case "0": role = "ADMIN"; break;
                        default: role = rawRole;
                    }
                } else {
                    // 不是有效的 JWT，从请求属性中获取角色
                    ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                    role = "ADMIN"; // 默认管理员角色
                    if (attributes != null) {
                        HttpServletRequest request = attributes.getRequest();
                        if (request != null) {
                            Object roleObj = request.getAttribute("role");
                            if (roleObj != null) {
                                role = roleObj.toString();
                            }
                        }
                    }
                }
            } else {
                // 从认证信息中获取，去掉ROLE_前缀
                role = authentication.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
            }
            
            return role;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(ResultCode.INTERNAL_SERVER_ERROR.getCode(), "获取用户角色失败: " + e.getMessage());
        }
    }
    
    /**
     * 判断当前用户是否是管理员
     * @return 是否是管理员
     */
    public static boolean isAdmin() {
        String role = getCurrentUserRole();
        return "ADMIN".equals(role);
    }
    
    /**
     * 判断当前用户是否是系统管理员（手机号为13800138000）
     * @return 是否是系统管理员
     */
    public static boolean isSystemAdmin() {
        try {
            Long currentUserId = getCurrentUserId();
            UserService userService = SpringContextHolder.getBean(UserService.class);
            User user = userService.getById(currentUserId);
            if (user != null && "13800138000".equals(user.getPhone())) {
                return true;
            }
            return false;
        } catch (Exception e) {
            System.out.println("SecurityUtil - 判断系统管理员失败: " + e.getMessage());
            return false;
        }
    }
}