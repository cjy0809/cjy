package com.community.center.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 场地WebSocket端点
 */
@ServerEndpoint("/api/ws/venue/{venueId}")
@Component
public class VenueWebSocketEndpoint {

    // 存储会话信息，key为场地ID，value为该场地的所有会话
    private static final Map<Long, ConcurrentHashMap<String, Session>> venueSessions = new ConcurrentHashMap<>();
    
    // 存储会话与场地ID的映射
    private static final Map<String, Long> sessionVenueMap = new ConcurrentHashMap<>();
    
    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("venueId") Long venueId) {
        System.out.println("WebSocket连接建立: venueId=" + venueId + ", sessionId=" + session.getId());
        
        // 将会话添加到对应场地的会话集合中
        venueSessions.computeIfAbsent(venueId, k -> new ConcurrentHashMap<>()).put(session.getId(), session);
        sessionVenueMap.put(session.getId(), venueId);
        
        // 发送欢迎消息
        sendMessage(session, createMessage("welcome", "连接成功", null));
    }
    
    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose(Session session) {
        String sessionId = session.getId();
        Long venueId = sessionVenueMap.get(sessionId);
        
        System.out.println("WebSocket连接关闭: venueId=" + venueId + ", sessionId=" + sessionId);
        
        // 从会话集合中移除
        if (venueId != null && venueSessions.containsKey(venueId)) {
            venueSessions.get(venueId).remove(sessionId);
            if (venueSessions.get(venueId).isEmpty()) {
                venueSessions.remove(venueId);
            }
        }
        sessionVenueMap.remove(sessionId);
    }
    
    /**
     * 收到客户端消息后调用的方法
     */
    @OnMessage
    public void onMessage(String message, Session session, @PathParam("venueId") Long venueId) {
        System.out.println("收到WebSocket消息: venueId=" + venueId + ", sessionId=" + session.getId() + ", message=" + message);
        
        try {
            // 解析消息
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> messageMap = mapper.readValue(message, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
            
            String type = (String) messageMap.get("type");
            
            // 处理心跳消息
            if ("heartbeat".equals(type)) {
                sendMessage(session, createMessage("heartbeat", "pong", null));
            }
        } catch (Exception e) {
            System.err.println("处理WebSocket消息失败: " + e.getMessage());
            sendMessage(session, createMessage("error", "消息处理失败", null));
        }
    }
    
    /**
     * 发生错误时调用
     */
    @OnError
    public void onError(Session session, Throwable error) {
        String sessionId = session.getId();
        Long venueId = sessionVenueMap.get(sessionId);
        
        System.err.println("WebSocket发生错误: venueId=" + venueId + ", sessionId=" + sessionId);
        error.printStackTrace();
        
        // 关闭会话
        try {
            session.close();
        } catch (IOException e) {
            System.err.println("关闭WebSocket会话失败: " + e.getMessage());
        }
        
        // 从会话集合中移除
        if (venueId != null && venueSessions.containsKey(venueId)) {
            venueSessions.get(venueId).remove(sessionId);
            if (venueSessions.get(venueId).isEmpty()) {
                venueSessions.remove(venueId);
            }
        }
        sessionVenueMap.remove(sessionId);
    }
    
    /**
     * 向指定场地发送消息
     */
    public static void sendMessageToVenue(Long venueId, String type, Object data) {
        if (!venueSessions.containsKey(venueId)) {
            return;
        }
        
        String message = createMessage(type, null, data);
        ConcurrentHashMap<String, Session> sessions = venueSessions.get(venueId);
        
        // 向该场地的所有会话发送消息
        sessions.forEach((sessionId, session) -> {
            sendMessage(session, message);
        });
    }
    
    /**
     * 向指定会话发送消息
     */
    private static void sendMessage(Session session, String message) {
        if (session == null || !session.isOpen()) {
            return;
        }
        
        try {
            synchronized (session) {
                session.getBasicRemote().sendText(message);
            }
        } catch (IOException e) {
            System.err.println("发送WebSocket消息失败: " + e.getMessage());
        }
    }
    
    /**
     * 创建消息
     */
    private static String createMessage(String type, String message, Object data) {
        Map<String, Object> messageMap = new HashMap<>();
        messageMap.put("type", type);
        
        if (message != null) {
            messageMap.put("message", message);
        }
        
        if (data != null) {
            messageMap.put("data", data);
        }
        
        messageMap.put("timestamp", System.currentTimeMillis());
        
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(messageMap);
        } catch (Exception e) {
            System.err.println("创建WebSocket消息失败: " + e.getMessage());
            return "{\"type\":\"error\",\"message\":\"消息创建失败\"}";
        }
    }
}