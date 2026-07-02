package com.vistara.tourist_tracking_system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmergencyAlertResponse {
    private Long id;
    private Long sessionId;
    private String alertType;
    private String alertStatus;
    private String priority;
    private String visitorName;
    private String visitorEmail;
    private String visitorPhone;
    private String emergencyContactName;
    private String emergencyContactPhone;
    private Double latitude;
    private Double longitude;
    private String message;
    private String resolutionNotes;
    private Long assignedRangerId;
    private String assignedRangerName;
    private LocalDateTime createdAt;
    private LocalDateTime respondedAt;
    private LocalDateTime resolvedAt;
    private Integer responseTimeSeconds;
}