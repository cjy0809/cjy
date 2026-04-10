package com.community.center.interceptor;

import com.community.center.common.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * JWT拦截器
 */
@Slf4j
@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Resource
    private JwtUtil jwtUtil;

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        // 从请求头中获取token
        String token = request.getHeader("Authorization");
        
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            
            try {
                // 验证token
                String username = jwtUtil.getUsernameFromToken(token);
                if (username != null) {
                    // 将用户信息存入请求域
                    request.setAttribute("username", username);
                    Long userId = jwtUtil.getUserIdFromToken(token);
                    if (userId != null) {
                        request.setAttribute("userId", userId);
                    }
                    String role = jwtUtil.getRoleFromToken(token);
                    if (role != null) {
                        request.setAttribute("role", role);
                    }
                    return true;
                }
            } catch (Exception e) {
                log.error("JWT验证失败: {}", e.getMessage());
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":401,\"message\":\"未授权，请登录\"}");
                return false;
            }
        }
        
        // 如果没有token，继续执行，让Spring Security决定是否需要认证
        return true;
    }
}