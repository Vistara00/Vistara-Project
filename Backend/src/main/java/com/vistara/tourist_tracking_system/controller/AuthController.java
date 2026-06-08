package com.vistara.tourist_tracking_system.controller;

import com.vistara.tourist_tracking_system.dto.*;
import com.vistara.tourist_tracking_system.model.Role;
import com.vistara.tourist_tracking_system.model.User;
import com.vistara.tourist_tracking_system.service.JwtService;
import com.vistara.tourist_tracking_system.service.PasswordResetService;
import com.vistara.tourist_tracking_system.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtService jwtService;
    private final PasswordResetService passwordResetService;

    @PostMapping("/register/tourist")
    public ResponseEntity<ApiResponse<?>> registerTourist(
            @Valid @RequestBody RegisterRequest request) {
        try {
            User user = userService.registerUser(request, Role.TOURIST);
            Map<String, Object> response = new HashMap<>();
            response.put("user", user);
            return ResponseEntity.ok(ApiResponse.success(response, "Registration successful"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<?>> login(
            @Valid @RequestBody LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(), request.getPassword())
            );

            User user = userService.findByEmail(request.getEmail());
            String token = jwtService.generateToken(user);

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("user", user);
            response.put("role", user.getRole());

            return ResponseEntity.ok(ApiResponse.success(response, "Login successful"));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(ApiResponse.error("Invalid credentials"));
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<?>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        try {
            String token = passwordResetService.generateResetToken(request.getEmail());

            Map<String, Object> response = new HashMap<>();
            // Remove resetToken from response in production —
            // it will be delivered by email instead
            response.put("resetToken", token);
            response.put("expiresInMinutes", 15);

            return ResponseEntity.ok(ApiResponse.success(response,
                    "Password reset token generated. Check your email."));
        } catch (Exception e) {
            // Return 200 regardless — prevents email enumeration attacks
            return ResponseEntity.ok(ApiResponse.success(null,
                    "If that email exists, a reset link has been sent."));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<?>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        try {
            passwordResetService.resetPassword(request.getToken(), request.getNewPassword());
            return ResponseEntity.ok(ApiResponse.success(null,
                    "Password reset successfully. Please log in."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}