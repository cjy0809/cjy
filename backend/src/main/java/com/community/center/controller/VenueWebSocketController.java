package com.community.center.controller;

import com.community.center.websocket.VenueWebSocketEndpoint;
import org.springframework.web.bind.annotation.RestController;

/**
 * 场地WebSocket控制器
 */
@RestController
public class VenueWebSocketController {
    
    /**
     * 发送场地状态更新消息
     */
    public void sendVenueStatusUpdate(Long venueId, Integer status) {
        VenueWebSocketEndpoint.sendMessageToVenue(venueId, "venue_status_update", status);
    }
    
    /**
     * 发送预约更新消息
     */
    public void sendReservationUpdate(Long venueId, Object reservationData) {
        VenueWebSocketEndpoint.sendMessageToVenue(venueId, "reservation_update", reservationData);
    }
}