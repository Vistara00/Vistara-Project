package com.vistara.tourist_tracking_system.dto;

import lombok.Data;
import com.vistara.tourist_tracking_system.model.GeofenceZone.ZoneType;
import java.time.LocalDateTime;

@Data
public class GeofenceZoneResponse {
    private Long id;
    private String zoneName;
    private String zoneDescription;
    private ZoneType zoneType;
    private String zoneBoundaryWkt;
    private String centerPointWkt;
    private Double radiusMeters;
    private Boolean alertOnEntry;
    private Boolean alertOnExit;
    private Boolean requiresEscort;
    private Integer maxVisitors;
    private Integer currentVisitors;
    private Boolean isActive;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}