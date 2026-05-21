package com.vistara.tourist_tracking_system.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;

@Data
public class LocationUpdateDTO {
    @NotNull
    private Double latitude;

    @NotNull
    private Double longitude;

    private Float accuracy;

    private Integer batteryLevel;

    private Long sessionId;
}