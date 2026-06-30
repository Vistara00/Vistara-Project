package com.vistara.tourist_tracking_system.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class EmergencyAlertResponse {
    private Long id;
    private String alertType;
    private String alertStatus;
    private String priority;
    private Double latitude;
    private Double longitude;
    private String message;

    // Visitor details
    private String visitorName;
    private String visitorPhone;

    // NEW: Emergency contact details
    private String emergencyContactName;
    private String emergencyContactPhone;

    private Long sessionId;
    private Long assignedRangerId;
    private String assignedRangerName;
    private String resolutionNotes;
    private LocalDateTime createdAt;
    private LocalDateTime respondedAt;
    private LocalDateTime resolvedAt;
    private Integer responseTimeSeconds;
}