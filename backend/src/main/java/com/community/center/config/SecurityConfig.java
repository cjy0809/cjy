package com.community.center.config;

import com.community.center.interceptor.JwtFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.annotation.Resource;

/**
 * Spring Security配置类
 * 配置系统的安全策略，包括JWT认证、权限控制、密码加密等
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Resource
    private JwtFilter jwtFilter;

    /**
     * 密码编码器Bean
     * 使用BCrypt算法对密码进行加密
     * @return 密码编码器实例
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 安全过滤器链配置
     * 配置HTTP安全策略，包括请求授权、JWT过滤器、会话管理等
     * @param http HttpSecurity对象
     * @return 安全过滤器链
     * @throws Exception 配置异常
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(new AntPathRequestMatcher("/auth/**")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/api/user/login")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/api/user/register")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/api/activity/**")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/api/user/**")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/api/favorites/**")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/api/service-item/**")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/api/venue/**")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/api/comment/**")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/api/payment/**")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/api/service-reservation/**")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/api/recharge/**")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/api/analytics/**")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/swagger-ui.html")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/swagger-resources/**")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/v2/api-docs")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/webjars/**")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/swagger-ui/**")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/uploads/**")).permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}
