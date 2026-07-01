package com.vistara.tourist_tracking_system.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminUpdateUserRequest {
    private String fullName;
    @Email private String email;
    @Pattern(regexp = "^[0-9]{10,12}$") private String phoneNumber;
    private String emergencyContactName;
    private String emergencyContactPhone;
    private Boolean active;
    private Boolean verified;
}