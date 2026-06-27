package com.vistara.tourist_tracking_system.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NotificationResponse {
    private Long id;
    private String title;
    private String message;
    private String type;
    private boolean read;
    private LocalDateTime createdAt;

    @JsonIgnore  // ← Hides from JSON response
    private boolean broadcast;

    @JsonIgnore  // ← Hides from JSON response
    private Long referenceId;
}