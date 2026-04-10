package com.community.center.interceptor;

import com.community.center.common.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Collections;

/**
 * JWT过滤器
 */
@Slf4j
@Component
public class JwtFilter implements Filter {

    @Resource
    private JwtUtil jwtUtil;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        
        // 从请求头中获取JWT令牌
        final String authorizationHeader = httpRequest.getHeader("Authorization");
        
        String username = null;
        String jwt = null;
        Long userId = null;
        String role = null;
        
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            try {
                username = jwtUtil.getUsernameFromToken(jwt);
                userId = jwtUtil.getUserIdFromToken(jwt);
                role = jwtUtil.getRoleFromToken(jwt);
            } catch (Exception e) {
                log.error("JWT令牌解析失败: {}", e.getMessage());
            }
        }
        
        // 如果JWT令牌有效，设置认证上下文
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // 将用户信息存储到请求属性中，供后续使用
            httpRequest.setAttribute("username", username);
            httpRequest.setAttribute("userId", userId);
            httpRequest.setAttribute("role", role);
            
            // 设置Spring Security认证上下文
            String authority = "ROLE_" + (role != null ? role : "USER");
            SimpleGrantedAuthority grantedAuthority = new SimpleGrantedAuthority(authority);
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    username, null, Collections.singletonList(grantedAuthority));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        
        chain.doFilter(request, response);
    }
}