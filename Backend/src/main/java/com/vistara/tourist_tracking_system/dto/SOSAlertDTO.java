package com.vistara.tourist_tracking_system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SOSAlertDTO {

    @NotNull(message = "Session ID is required")
    private Long sessionId;

    @NotBlank(message = "Alert type is required")
    private String alertType;

    @NotNull(message = "Latitude is required")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    private Double longitude;

    private String message;

    // Optional fields for additional context
    private Double accuracy;
    private Double batteryLevel;
    private String deviceInfo;
    private String visitorNotes;
}