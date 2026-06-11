package com.vistara.tourist_tracking_system.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdminCheckOutRequest {
    @NotNull(message = "Session ID is required")
    private Long sessionId;

    private String notes;
}