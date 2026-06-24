package com.vistara.tourist_tracking_system.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LocationUpdateDTO {
    @NotNull
    private Double latitude;

    @NotNull
    private Double longitude;

    private Float accuracy;        // GPS accuracy in meters

    private Integer batteryLevel;  // Device battery percentage

    private Long sessionId;        // Active visitor session ID
}