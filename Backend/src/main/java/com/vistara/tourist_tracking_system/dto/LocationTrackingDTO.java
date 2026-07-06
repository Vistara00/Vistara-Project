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
public class LocationTrackingDTO {
    private Long id;
    private Double latitude;
    private Double longitude;
    private Float accuracy;
    private Integer batteryLevel;
    private LocalDateTime timestamp;
    private Boolean withinGeofence;
}