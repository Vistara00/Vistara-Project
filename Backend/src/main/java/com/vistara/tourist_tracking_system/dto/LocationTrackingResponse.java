package com.vistara.tourist_tracking_system.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class LocationTrackingResponse {
    private Long id;
    private Long sessionId;
    private Double latitude;
    private Double longitude;
    private Float accuracy;
    private Integer batteryLevel;
    private boolean withinGeofence;
    private LocalDateTime timestamp;
    private LocalDateTime createdAt;
}