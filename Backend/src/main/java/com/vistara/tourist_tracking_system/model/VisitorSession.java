package com.vistara.tourist_tracking_system.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.locationtech.jts.geom.Point;
import java.time.LocalDateTime;

@Entity
@Table(name = "visitor_sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VisitorSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "check_in_time", nullable = false)
    private LocalDateTime checkInTime;

    @Column(name = "check_out_time")
    private LocalDateTime checkOutTime;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "group_size")
    private Integer groupSize = 1;

    @Column(name = "vehicle_registration")
    private String vehicleRegistration;

    // FIX: mapped as JTS Point — matches the PostGIS geometry(POINT,4326) column
    // Requires hibernate-spatial dependency in pom.xml
    @Column(name = "last_known_location", columnDefinition = "geometry(Point,4326)")
    private Point lastKnownLocation;

    @Column(name = "last_location_update")
    private LocalDateTime lastLocationUpdate;

    @Column(name = "sos_triggered")
    private boolean sosTriggered = false;

    @Column(name = "has_emergency")
    private boolean hasEmergency = false;

    @Column(name = "notes")
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (checkInTime == null) {
            checkInTime = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}