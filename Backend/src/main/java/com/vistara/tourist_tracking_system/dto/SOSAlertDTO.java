package com.vistara.tourist_tracking_system.dto;

import com.vistara.tourist_tracking_system.model.EmergencyAlert.AlertType;
import lombok.Data;
import jakarta.validation.constraints.NotNull;

@Data
public class SOSAlertDTO {
    @NotNull
    private AlertType alertType;

    private String message;

    @NotNull
    private Double latitude;

    @NotNull
    private Double longitude;

    private Long sessionId;
}