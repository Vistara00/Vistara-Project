package com.vistara.tourist_tracking_system.dto;

import com.vistara.tourist_tracking_system.model.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;

    @NotBlank(message = "Phone number is required")
    private String phoneNumber;

    private String nationalId;

    // ✅ Use User.Role instead of Role
    private User.Role role = User.Role.TOURIST;  // Default to TOURIST

    private String emergencyContactName;
    private String emergencyContactPhone;
}