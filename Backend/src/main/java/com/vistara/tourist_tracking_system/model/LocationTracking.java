package com.vistara.tourist_tracking_system.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.geo.Point;

import java.time.LocalDateTime;

@Entity
@Table(name = "location_tracking")
@Data
@NoArgsConstructor
@AllArgsConstructor
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

    @Column(name = "accuracy")
    private Float accuracy;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "is_within_geofence")
    private boolean withinGeofence = true;

    @Column(name = "battery_level")
    private Integer batteryLevel;
}