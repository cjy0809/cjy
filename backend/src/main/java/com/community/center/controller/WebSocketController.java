package com.community.center.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Objects;

/**
 * WebSocket控制器
 */
@Controller
public class WebSocketController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * 向特定用户发送消息
     */
    public void sendToUser(Long userId, String destination, Object message) {
        Objects.requireNonNull(userId, "用户ID不能为空");
        Objects.requireNonNull(destination, "目标地址不能为空");
        Objects.requireNonNull(message, "消息内容不能为空");
        messagingTemplate.convertAndSendToUser(String.valueOf(userId), destination, message);
    }

    /**
     * 向所有管理员广播消息
     */
    public void broadcastToAdmins(String destination, Object message) {
        Objects.requireNonNull(destination, "目标地址不能为空");
        Objects.requireNonNull(message, "消息内容不能为空");
        messagingTemplate.convertAndSend("/topic/admin" + destination, message);
    }
}