package com.vistara.tourist_tracking_system.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class BookingResponse {
    private Long sessionId;
    private String visitorName;
    private String visitorEmail;
    private LocalDateTime checkInTime;
    private Integer groupSize;
    private String paymentMethod;
    private BigDecimal amount;
    private String paymentReference;
    private Boolean isPaid;
    private String bookingNotes;
}