package com.vistara.tourist_tracking_system.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String fullName;
    @Email(message = "Invalid email format")
    private String email;
    @Pattern(regexp = "^[0-9]{10,12}$", message = "Phone number must be 10-12 digits")
    private String phoneNumber;
    private String nationalId;
    private String emergencyContactName;
    private String emergencyContactPhone;
}