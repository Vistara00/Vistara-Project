package com.vistara.tourist_tracking_system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponse {
    // Basic booking info
    private Long id;
    private String bookingReference;

    // User info
    private Long userId;
    private String userFullName;
    private String userEmail;
    private String userPhoneNumber;

    // Booking details
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private Integer groupSize;
    private String vehicleRegistration;
    private String paymentMethod;
    private BigDecimal amount;
    private String paymentReference;

    // Status fields
    private String paymentStatus;
    private String bookingStatus;
    private Boolean checkinStatus;  // ✅ Track if booking has been checked in

    // Audit fields
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Admin fields
    private String adminNotes;
    private String paymentTrackingId;

    // ✅ QR code field for check-in verification
    private String qrCodeBase64;
}