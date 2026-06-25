package com.vistara.tourist_tracking_system.model;

import jakarta.persistence.*;
import lombok.Data;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import java.time.LocalDateTime;

@Entity
@Table(name = "geofence_zones")
@Data
public class GeofenceZone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "zone_name", nullable = false)
    private String zoneName;

    @Column(name = "zone_description")
    private String zoneDescription;

    @Enumerated(EnumType.STRING)
    @Column(name = "zone_type", nullable = false)
    private ZoneType zoneType;

    @Column(name = "zone_boundary", columnDefinition = "geometry(Polygon,4326)", nullable = false)
    private Polygon zoneBoundary;

    @Column(name = "center_point", columnDefinition = "geometry(Point,4326)")
    private Point centerPoint;

    @Column(name = "radius_meters")
    private Double radiusMeters;

    @Column(name = "alert_on_entry")
    private Boolean alertOnEntry = true;

    @Column(name = "alert_on_exit")
    private Boolean alertOnExit = false;

    @Column(name = "requires_escort")
    private Boolean requiresEscort = false;

    @Column(name = "max_visitors")
    private Integer maxVisitors;

    @Column(name = "current_visitors")
    private Integer currentVisitors = 0;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum ZoneType {
        SAFE, RESTRICTED, DANGER, WILDLIFE_AREA, EMERGENCY_EXIT
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}