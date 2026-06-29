package com.vistara.tourist_tracking_system.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BroadcastRequest {
    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Message is required")
    private String message;

    private Long userId;  // Optional: send to specific user

    // Broadcast type
    private BroadcastType broadcastType = BroadcastType.ALL_USERS;

    public enum BroadcastType {
        ALL_USERS,      // All users in the system
        ACTIVE_VISITORS // Only visitors with active sessions
    }
}