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
    private Long id;
    private String bookingReference;
    private Long userId;
    private String userFullName;
    private String userEmail;
    private String userPhoneNumber;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private Integer groupSize;
    private String vehicleRegistration;
    private String paymentMethod;
    private BigDecimal amount;
    private String paymentReference;
    private String paymentStatus;
    private String bookingStatus;
    private LocalDateTime createdAt;

    // ✅ Add QR code field
    private String qrCodeBase64;
}