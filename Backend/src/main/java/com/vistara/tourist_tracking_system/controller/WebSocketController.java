package com.vistara.tourist_tracking_system.controller;

import com.vistara.tourist_tracking_system.dto.EmergencyAlertResponse;
import com.vistara.tourist_tracking_system.dto.LocationUpdateDTO;
import com.vistara.tourist_tracking_system.dto.SOSAlertDTO;
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
    public void handleSOS(SOSAlertDTO sosDTO) {
        // Trigger SOS and get the response DTO
        EmergencyAlertResponse savedAlert = emergencyService.triggerSOS(sosDTO);

        // Broadcast to all subscribers
        messagingTemplate.convertAndSend("/topic/alerts", savedAlert);

        // Send to admin specifically
        messagingTemplate.convertAndSendToUser("admin", "/queue/alerts", savedAlert);
    }
}