package com.vistara.tourist_tracking_system.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class BookingRequest {
    @NotNull(message = "Check-in date is required")
    private LocalDate checkInDate;

    @NotNull(message = "Check-out date is required")
    private LocalDate checkOutDate;

    @Positive(message = "Group size must be at least 1")
    private Integer groupSize = 1;

    private String vehicleRegistration;

    @NotBlank(message = "Payment method is required")
    private String paymentMethod; // "MPESA" or "E_CITIZEN"

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private BigDecimal amount;
}