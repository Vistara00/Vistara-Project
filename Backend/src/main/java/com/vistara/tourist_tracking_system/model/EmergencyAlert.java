package com.vistara.tourist_tracking_system.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;

import java.time.LocalDateTime;

@Entity
@Table(name = "emergency_alerts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmergencyAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "session_id", nullable = false)
    private VisitorSession session;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "alert_type", nullable = false)
    private AlertType alertType;

    @Enumerated(EnumType.STRING)
    @Column(name = "alert_status", nullable = false)
    private AlertStatus alertStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority")
    private AlertPriority priority;

    @Column(name = "latitude", nullable = false)
    private Double latitude;

    @Column(name = "longitude", nullable = false)
    private Double longitude;

    @Column(name = "location_point", columnDefinition = "geometry(Point,4326)", insertable = false, updatable = false)
    @JsonIgnore
    private Point locationPoint;

    @Column(name = "message", length = 500)
    private String message;

    @ManyToOne
    @JoinColumn(name = "assigned_ranger_id")
    private User assignedRanger;

    @Column(name = "responded_at")
    private LocalDateTime respondedAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "resolution_notes")
    private String resolutionNotes;

    @Column(name = "response_time_seconds")
    private Integer responseTimeSeconds;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (alertStatus == null) alertStatus = AlertStatus.PENDING;
        if (priority == null) priority = AlertPriority.HIGH;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        if (respondedAt != null && responseTimeSeconds == null) {
            responseTimeSeconds = (int) java.time.Duration
                    .between(createdAt, respondedAt).getSeconds();
        }
    }

    public enum AlertType {
        MEDICAL,
        LOST,
        WILDLIFE_ENCOUNTER,
        VEHICLE_BREAKDOWN,
        ACCIDENT,
        GENERAL_DISTRESS
    }

    public enum AlertStatus {
        PENDING,
        RESPONDING,
        RESOLVED,
        FALSE_ALARM
    }

    public enum AlertPriority {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }
}