package com.vistara.tourist_tracking_system.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class MpesaStkRequest {
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^254[0-9]{9}$", message = "Phone must be in format 2547XXXXXXXX")
    private String phoneNumber;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be greater than 0")
    private Integer amount;

    @NotBlank(message = "Account reference is required")
    private String accountReference;  // e.g., Booking ID

    @NotBlank(message = "Transaction description is required")
    private String transactionDesc;
}