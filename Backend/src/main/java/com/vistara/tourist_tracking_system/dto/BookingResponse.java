package com.vistara.tourist_tracking_system.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class BookingResponse {
    private Long id;
    private String bookingReference;
    private Long userId;
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
}