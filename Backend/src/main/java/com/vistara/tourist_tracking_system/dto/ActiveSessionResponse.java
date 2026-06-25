package com.vistara.tourist_tracking_system.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ActiveSessionResponse {
    // Session information
    private Long sessionId;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
    private boolean active;
    private Integer groupSize;
    private String vehicleRegistration;
    private boolean sosTriggered;
    private boolean hasEmergency;
    private String notes;

    // Visitor information (limited)
    private VisitorInfo visitor;

    // Booking information (limited)
    private BookingInfo booking;

    @Data
    public static class VisitorInfo {
        private Long id;
        private String fullName;
        private String email;
        private String phoneNumber;
        private String emergencyContactName;
        private String emergencyContactPhone;
    }

    @Data
    public static class BookingInfo {
        private Long id;
        private String bookingReference;
        private LocalDate checkInDate;
        private LocalDate checkOutDate;
        private String paymentMethod;
        private String paymentStatus;
        private String bookingStatus;
        private BigDecimal amount;
    }
}