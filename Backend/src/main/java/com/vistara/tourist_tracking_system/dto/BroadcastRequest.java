package com.vistara.tourist_tracking_system.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BroadcastRequest {
    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Message is required")
    private String message;

    private Long userId;  // Optional: send to specific user, null = broadcast to all
}