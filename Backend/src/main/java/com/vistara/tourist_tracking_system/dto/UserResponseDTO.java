package com.vistara.tourist_tracking_system.dto;

import lombok.Data;

@Data
public class UserResponseDTO {
    private Long id;
    private String email;
    private String fullName;
    private String phoneNumber;
    private String role;
    private String emergencyContactName;
    private String emergencyContactPhone;
    private String nationalId;
    private boolean active;
    private boolean verified;
}