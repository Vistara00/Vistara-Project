package com.vistara.tourist_tracking_system.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class VisitorSessionResponse {
    private Long sessionId;  // Explicitly named
    private Long userId;
    private String visitorName;
    private String visitorEmail;
    private String visitorPhone;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
    private boolean active;
    private Integer groupSize;
    private String vehicleRegistration;
    private Boolean sosTriggered;
    private Boolean hasEmergency;
    private String notes;
    private Long bookingId;
    private String bookingReference;
    private String bookingStatus;
    private String paymentStatus;
}