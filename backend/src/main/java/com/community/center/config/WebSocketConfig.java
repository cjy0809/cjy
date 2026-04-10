package com.community.center.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket配置类
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(@NonNull MessageBrokerRegistry config) {
        // 配置消息代理，指定消息的前缀
        config.enableSimpleBroker("/topic", "/queue");
        // 配置应用程序的消息前缀
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(@NonNull StompEndpointRegistry registry) {
        // 注册STOMP端点
        registry.addEndpoint("/ws/emergency")
                .setAllowedOriginPatterns("*")
                .withSockJS();
        
        // 注册场地预约WebSocket端点
        registry.addEndpoint("/ws/venue")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }
}