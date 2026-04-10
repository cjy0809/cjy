package com.community.center.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * WebSocket消息实体类
 */
@Data
public class WebSocketMessage<T> {

    /**
     * 消息类型
     */
    private String type;

    /**
     * 消息内容
     */
    private T content;

    /**
     * 发送时间
     */
    private LocalDateTime timestamp;

    /**
     * 发送者ID
     */
    private Long senderId;

    /**
     * 接收者ID
     */
    private Long receiverId;

    /**
     * 是否广播给所有管理员
     */
    private boolean broadcastToAdmins;

    public WebSocketMessage() {
        this.timestamp = LocalDateTime.now();
    }

    public WebSocketMessage(String type, T content) {
        this();
        this.type = type;
        this.content = content;
    }

    public WebSocketMessage(String type, T content, Long senderId, Long receiverId) {
        this(type, content);
        this.senderId = senderId;
        this.receiverId = receiverId;
    }

    public WebSocketMessage(String type, T content, Long senderId, boolean broadcastToAdmins) {
        this(type, content);
        this.senderId = senderId;
        this.broadcastToAdmins = broadcastToAdmins;
    }
}