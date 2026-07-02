package com.vistara.tourist_tracking_system.dto;

import com.vistara.tourist_tracking_system.model.User;
import lombok.Data;

@Data
public class UserResponseDTO {
    private Long id;
    private String email;
    private String fullName;
    private String phoneNumber;
    private String nationalId;
    private String role;  // Changed to String to avoid conflict
    private String emergencyContactName;
    private String emergencyContactPhone;
    private boolean active;
    private boolean verified;

    // Helper method to set role from enum
    public void setRole(User.Role role) {
        this.role = role != null ? role.name() : null;
    }

    // Helper method to get role as enum
    public User.Role getRoleEnum() {
        return role != null ? User.Role.valueOf(role) : null;
    }
}