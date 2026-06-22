package com.vistara.tourist_tracking_system.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NotificationResponse {
    private Long id;
    private String title;
    private String message;
    private String type;
    private boolean read;
    private boolean broadcast;
    private Long referenceId;
    private LocalDateTime createdAt;
}