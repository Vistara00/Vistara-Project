package com.vistara.tourist_tracking_system.controller;

import com.vistara.tourist_tracking_system.dto.LocationUpdateDTO;
import com.vistara.tourist_tracking_system.model.EmergencyAlert;
import com.vistara.tourist_tracking_system.service.EmergencyService;
import com.vistara.tourist_tracking_system.service.TrackingService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class WebSocketController {

    private final TrackingService trackingService;
    private final EmergencyService emergencyService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/location")
    @SendTo("/topic/locations")
    public LocationUpdateDTO broadcastLocation(LocationUpdateDTO location) {
        trackingService.updateLocation(location);
        return location;
    }

    @MessageMapping("/sos")
    public void handleSOS(EmergencyAlert alert) {
        EmergencyAlert savedAlert = emergencyService.triggerSOS(
                new com.vistara.tourist_tracking_system.dto.SOSAlertDTO()
        );
        messagingTemplate.convertAndSend("/topic/alerts", savedAlert);
        messagingTemplate.convertAndSendToUser("admin", "/queue/alerts", savedAlert);
    }
}