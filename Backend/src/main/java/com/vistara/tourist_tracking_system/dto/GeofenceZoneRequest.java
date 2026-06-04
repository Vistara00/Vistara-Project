package com.vistara.tourist_tracking_system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import com.vistara.tourist_tracking_system.model.GeofenceZone.ZoneType;

@Data
public class GeofenceZoneRequest {

    @NotBlank(message = "Zone name is required")
    private String zoneName;

    private String zoneDescription;

    @NotNull(message = "Zone type is required")
    private ZoneType zoneType;

    @NotBlank(message = "WKT polygon is required")
    private String zoneBoundaryWkt;  // WKT format e.g., "POLYGON((x1 y1, x2 y2, ...))"

    private String centerPointWkt;   // WKT format e.g., "POINT(x y)"

    private Double radiusMeters;

    private Boolean alertOnEntry = true;
    private Boolean alertOnExit = false;
    private Boolean requiresEscort = false;
    private Integer maxVisitors;
    private Boolean isActive = true;
}