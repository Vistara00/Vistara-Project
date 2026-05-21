package com.vistara.tourist_tracking_system.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
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
    private boolean isActive = true;

    @Column(name = "group_size")
    private Integer groupSize = 1;

    @Column(name = "vehicle_registration")
    private String vehicleRegistration;

    @Column(name = "last_known_location")
    private String lastKnownLocation; // Stored as GeoJSON string

    @Column(name = "sos_triggered")
    private boolean sosTriggered = false;
}