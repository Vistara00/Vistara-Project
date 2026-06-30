package com.vistara.tourist_tracking_system.controller;

import com.vistara.tourist_tracking_system.dto.ApiResponse;
import com.vistara.tourist_tracking_system.dto.EmergencyAlertResponse;
import com.vistara.tourist_tracking_system.dto.SOSAlertDTO;
import com.vistara.tourist_tracking_system.service.EmergencyService;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/emergency")
@RequiredArgsConstructor
public class EmergencyController {

    private final EmergencyService emergencyService;

    @PostMapping("/sos")
    public ResponseEntity<ApiResponse<EmergencyAlertResponse>> triggerSOS(
            @Valid @RequestBody SOSAlertDTO dto) {
        EmergencyAlertResponse alert = emergencyService.triggerSOS(dto);
        return ResponseEntity.ok(ApiResponse.success(alert, "SOS alert sent successfully"));
    }

    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<EmergencyAlertResponse>>> getPendingAlerts() {
        List<EmergencyAlertResponse> alerts = emergencyService.getPendingAlerts();
        return ResponseEntity.ok(ApiResponse.success(alerts, "Pending alerts retrieved"));
    }

    /**
     * Get all emergency alerts regardless of status (admin only)
     * Includes emergency contact details
     */
    @GetMapping("/admin/all")
    public ResponseEntity<ApiResponse<List<EmergencyAlertResponse>>> getAllAlerts(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails adminDetails) {
        List<EmergencyAlertResponse> alerts = emergencyService.getAllAlerts();
        log.info("Admin {} retrieved all emergency alerts. Total: {}", adminDetails.getUsername(), alerts.size());
        return ResponseEntity.ok(ApiResponse.success(alerts, "All emergency alerts retrieved successfully"));
    }
}