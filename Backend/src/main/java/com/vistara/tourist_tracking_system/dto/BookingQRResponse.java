package com.vistara.tourist_tracking_system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingQRResponse {
    private Long bookingId;
    private String bookingReference;
    private String qrCodeBase64;
    private String visitorName;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private String paymentStatus;
    private String bookingStatus;
}