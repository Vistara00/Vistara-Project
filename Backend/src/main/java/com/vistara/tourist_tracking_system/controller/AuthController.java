package com.vistara.tourist_tracking_system.controller;

import com.vistara.tourist_tracking_system.dto.*;
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
            User user = userService.registerUser(request, User.Role.TOURIST);
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

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<?>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        try {
            passwordResetService.generateResetToken(request.getEmail());
            return ResponseEntity.ok(ApiResponse.success(null, "If that email exists, a reset link has been sent."));
        } catch (Exception e) {
            log.error("Password reset request failed for {}: {}", request.getEmail(), e.getMessage());
            return ResponseEntity.ok(ApiResponse.success(null, "If that email exists, a reset link has been sent."));
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

    @PostMapping("/register/admin")
    public ResponseEntity<ApiResponse<?>> registerAdmin(@Valid @RequestBody RegisterRequest request) {
        try {
            User user = userService.registerUser(request, User.Role.ADMIN);
            Map<String, Object> response = new HashMap<>();
            response.put("user", user);
            response.put("message", "Admin registered successfully");
            return ResponseEntity.ok(ApiResponse.success(response, "Registration successful"));
        } catch (Exception e) {
            log.error("Admin registration failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/register/ranger")
    public ResponseEntity<ApiResponse<?>> registerRanger(@Valid @RequestBody RegisterRequest request) {
        try {
            // Use PARK_RANGER role for ranger registration
            User user = userService.registerUser(request, User.Role.PARK_RANGER);
            Map<String, Object> response = new HashMap<>();
            response.put("user", user);
            response.put("message", "Ranger registered successfully");
            return ResponseEntity.ok(ApiResponse.success(response, "Registration successful"));
        } catch (Exception e) {
            log.error("Ranger registration failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}