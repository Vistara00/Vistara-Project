package com.vistara.tourist_tracking_system.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class AdminUpdateUserRequest {
    private Long userId;          // or use email
    private String fullName;
    @Email private String email;
    @Pattern(regexp = "^[0-9]{10,12}$") private String phoneNumber;
    private String emergencyContactName;
    private String emergencyContactPhone;
    private Boolean active;       // admin can activate/deactivate
    private Boolean verified;
}