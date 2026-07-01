package com.vistara.tourist_tracking_system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentStatusResponse {
    private Long bookingId;
//    private String bookingReference;
    private String paymentStatus;  // PAID, PENDING, FAILED
//    private String bookingStatus;  // CONFIRMED, PENDING, CANCELLED
//    private String paymentReference;
//    private BigDecimal amount;
}