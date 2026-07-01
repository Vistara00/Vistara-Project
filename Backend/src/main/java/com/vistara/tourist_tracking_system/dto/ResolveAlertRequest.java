package com.vistara.tourist_tracking_system.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResolveAlertRequest {
    @NotBlank(message = "Resolution notes are required")
    private String notes;
}