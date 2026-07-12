package com.vistara.tourist_tracking_system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QRCodeResponse {
    private Long bookingId;
    private String bookingReference;
    private String qrCodeBase64;
    private String visitorName;
    private String checkInDate;
    private String checkOutDate;
    private String paymentStatus;
    private String bookingStatus;
}