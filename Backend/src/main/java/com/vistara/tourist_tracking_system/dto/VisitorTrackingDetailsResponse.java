package com.vistara.tourist_tracking_system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VisitorTrackingDetailsResponse {

    // Session details
    private Long sessionId;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
    private Boolean isActive;
    private Integer groupSize;
    private String vehicleRegistration;
    private Boolean hasEmergency;
    private Boolean sosTriggered;
    private String notes;

    // Visitor details
    private Long visitorId;
    private String visitorName;
    private String visitorEmail;
    private String visitorPhone;

    // Booking details
    private Long bookingId;
    private String bookingReference;
    private String paymentStatus;
    private String bookingStatus;

    // Last location
    private Double lastLatitude;
    private Double lastLongitude;
    private LocalDateTime lastLocationTime;

    // Location history
    private List<LocationTrackingDTO> locationHistory;
    private Integer totalLocations;
}