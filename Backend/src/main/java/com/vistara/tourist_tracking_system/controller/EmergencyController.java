package com.vistara.tourist_tracking_system.controller;

import com.vistara.tourist_tracking_system.dto.ApiResponse;
import com.vistara.tourist_tracking_system.dto.SOSAlertDTO;
import com.vistara.tourist_tracking_system.model.EmergencyAlert;
import com.vistara.tourist_tracking_system.service.EmergencyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/emergency")
@RequiredArgsConstructor
public class EmergencyController {

    private final EmergencyService emergencyService;

    @PostMapping("/sos")
    public ResponseEntity<ApiResponse<?>> triggerSOS(@Valid @RequestBody SOSAlertDTO dto) {
        EmergencyAlert alert = emergencyService.triggerSOS(dto);
        return ResponseEntity.ok(ApiResponse.success(alert, "SOS alert sent successfully"));
    }

    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<?>> getPendingAlerts() {
        return ResponseEntity.ok(ApiResponse.success(emergencyService.getPendingAlerts(), "Pending alerts retrieved"));
    }
}