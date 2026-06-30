package com.vistara.tourist_tracking_system.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vistara.tourist_tracking_system.model.Notification;  // ← ADD THIS IMPORT
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

    // ===== USER-VISIBLE FIELDS =====
    private Long id;
    private String title;
    private String message;
    private String type;
    private boolean read;
    private LocalDateTime createdAt;

    // ===== ADMIN-ONLY FIELDS (hidden from regular users) =====

    @JsonIgnore
    private boolean broadcast;

    @JsonIgnore
    private Long referenceId;

    // ===== STATIC FACTORY METHODS =====

    /**
     * Create a user-facing notification response (hides broadcast and referenceId)
     */
    public static NotificationResponse forUser(Notification notification) {
        if (notification == null) {
            return null;
        }
        NotificationResponse response = new NotificationResponse();
        response.setId(notification.getId());
        response.setTitle(notification.getTitle());
        response.setMessage(notification.getMessage());
        response.setType(notification.getType());
        response.setRead(notification.isRead());
        response.setCreatedAt(notification.getCreatedAt());
        // broadcast and referenceId remain null (hidden via @JsonIgnore)
        return response;
    }

    /**
     * Create an admin-facing notification response (shows all fields)
     */
    public static NotificationResponse forAdmin(Notification notification) {
        if (notification == null) {
            return null;
        }
        NotificationResponse response = new NotificationResponse();
        response.setId(notification.getId());
        response.setTitle(notification.getTitle());
        response.setMessage(notification.getMessage());
        response.setType(notification.getType());
        response.setRead(notification.isRead());
        response.setCreatedAt(notification.getCreatedAt());
        response.setBroadcast(notification.isBroadcast());
        response.setReferenceId(notification.getReferenceId());
        return response;
    }
}