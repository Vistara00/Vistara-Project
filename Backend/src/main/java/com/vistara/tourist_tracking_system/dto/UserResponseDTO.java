package com.vistara.tourist_tracking_system.dto;

import com.vistara.tourist_tracking_system.model.User;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserResponseDTO {
    private Long id;
    private String email;
    private String fullName;
    private String phoneNumber;
    private String nationalId;
    private User.Role role;
    private String emergencyContactName;
    private String emergencyContactPhone;
    private boolean active;
    private boolean verified;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Helper method to set role from enum
    public void setRole(User.Role role) {
        this.role = role;
    }

    // Helper method to get role as string if needed
    public String getRoleAsString() {
        return role != null ? role.name() : null;
    }
}