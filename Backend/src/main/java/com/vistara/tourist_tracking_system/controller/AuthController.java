package com.vistara.tourist_tracking_system.controller;

import com.vistara.tourist_tracking_system.dto.*;
import com.vistara.tourist_tracking_system.model.Role;
import com.vistara.tourist_tracking_system.model.User;
import com.vistara.tourist_tracking_system.service.JwtService;
import com.vistara.tourist_tracking_system.service.PasswordResetService;
import com.vistara.tourist_tracking_system.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtService jwtService;
    private final PasswordResetService passwordResetService;

    @PostMapping("/register/tourist")
    public ResponseEntity<ApiResponse<?>> registerTourist(@Valid @RequestBody RegisterRequest request) {
        try {
            User user = userService.registerUser(request, Role.TOURIST);
            Map<String, Object> response = new HashMap<>();
            response.put("user", user);
            response.put("message", "Tourist registered successfully");
            return ResponseEntity.ok(ApiResponse.success(response, "Registration successful"));
        } catch (Exception e) {
            log.error("Registration failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<?>> login(@Valid @RequestBody LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            User user = userService.findByEmail(request.getEmail());
            String token = jwtService.generateToken(user);

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("user", user);
            response.put("role", user.getRole());

            return ResponseEntity.ok(ApiResponse.success(response, "Login successful"));
        } catch (Exception e) {
            log.warn("Failed login attempt for email: {}", request.getEmail());
            return ResponseEntity.status(401).body(ApiResponse.error("Invalid credentials"));
        }
    }

//    @PostMapping("/forgot-password")
//    public ResponseEntity<ApiResponse<?>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
//        try {
//            // This method will send an email with a 6-digit token
//            // It returns the token only for testing – remove in production
//            String token = passwordResetService.generateResetToken(request.getEmail());
//
//            // For production, do NOT return the token in response
//            // Only return success message
//            // For debugging, you can keep token but remove before deployment
//            Map<String, Object> response = new HashMap<>();
//            // ⚠️ Remove this line in production – token should be email-only
//            response.put("resetToken", token);
//            response.put("expiresInMinutes", 15);
//
//            log.info("Password reset token generated for email: {}", request.getEmail());
//            return ResponseEntity.ok(ApiResponse.success(response,
//                    "If that email exists, a reset link has been sent."));
//        } catch (Exception e) {
//            // Log the actual error for debugging (don't expose to client)
//            log.error("Password reset request failed for {}: {}", request.getEmail(), e.getMessage());
//            // Still return 200 to avoid user enumeration
//            return ResponseEntity.ok(ApiResponse.success(null,
//                    "If that email exists, a reset link has been sent."));
//        }
//    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<?>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        try {
            String token = passwordResetService.generateResetToken(request.getEmail());
            Map<String, Object> response = new HashMap<>();
            response.put("resetToken", token);
            response.put("expiresInMinutes", 15);
            return ResponseEntity.ok(ApiResponse.success(response, "Token generated (dev mode)"));
        } catch (Exception e) {
            // Return the actual error for debugging
            return ResponseEntity.status(500).body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<?>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            passwordResetService.resetPassword(request.getToken(), request.getNewPassword());
            return ResponseEntity.ok(ApiResponse.success(null, "Password reset successfully"));
        } catch (Exception e) {
            log.error("Password reset failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}