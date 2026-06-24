package com.vistara.tourist_tracking_system.model;

import jakarta.persistence.*;
import lombok.Data;
import org.locationtech.jts.geom.Point;
import java.time.LocalDateTime;

@Entity
@Table(name = "location_tracking")
@Data
public class LocationTracking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "session_id", nullable = false)
    private VisitorSession session;

    @Column(name = "latitude", nullable = false)
    private Double latitude;

    @Column(name = "longitude", nullable = false)
    private Double longitude;

    @Column(name = "location_point", columnDefinition = "geometry(Point,4326)")
    private Point locationPoint;  // PostGIS geometry point

    @Column(name = "accuracy")
    private Float accuracy;

    @Column(name = "battery_level")
    private Integer batteryLevel;

    @Column(name = "is_within_geofence")
    private boolean withinGeofence = true;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
        createdAt = LocalDateTime.now();
    }
}