package com.vistara.tourist_tracking_system.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
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

    @Column(name = "alert_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private AlertType alertType;

    @Column(name = "latitude", nullable = false)
    private Double latitude;

    @Column(name = "longitude", nullable = false)
    private Double longitude;

    @Column(name = "message", length = 500)
    private String message;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private AlertStatus status = AlertStatus.PENDING;

    @ManyToOne
    @JoinColumn(name = "assigned_ranger_id")
    private User assignedRanger;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "resolution_notes")
    private String resolutionNotes;

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
}