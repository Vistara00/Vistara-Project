package com.vistara.tourist_tracking_system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Lean response for GET /admin/active-visitors.
 * Flattens the fields the check-out interface actually needs (booking + user
 * info at top level) and deliberately omits the nested User/Booking entities
 * so things like the password hash never leave the server.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActiveVisitorResponse {

    private Long id;                    // VisitorSession id — used as sessionId for /admin/checkout
    private String bookingReference;
    private String userFullName;
    private String userEmail;
    private String userPhoneNumber;
    private BigDecimal amount;          // adjust to double if Booking.amount isn't BigDecimal
    private LocalDate checkInDate;      // adjust to LocalDateTime/String if Booking uses a different type
    private LocalDate checkOutDate;
    private String bookingStatus;
    private String vehicleRegistration;
    private Integer groupSize;
    private LocalDateTime checkInTime;
    private boolean sosTriggered;
    private boolean hasEmergency;
    private String notes;
}