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
    private String visitorName;
    private String visitorPhone;
    private Long sessionId;
    private Long assignedRangerId;
    private String assignedRangerName;
    private String resolutionNotes;    // ← Make sure this field exists
    private LocalDateTime createdAt;
    private LocalDateTime respondedAt;
    private LocalDateTime resolvedAt;
    private Integer responseTimeSeconds;
}