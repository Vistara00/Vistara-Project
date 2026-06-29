package com.vistara.tourist_tracking_system.controller;

import com.vistara.tourist_tracking_system.dto.ApiResponse;
import com.vistara.tourist_tracking_system.dto.EmergencyAlertResponse;
import com.vistara.tourist_tracking_system.dto.SOSAlertDTO;
import com.vistara.tourist_tracking_system.service.EmergencyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
}