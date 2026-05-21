package com.vistara.tourist_tracking_system.controller;

import com.vistara.tourist_tracking_system.dto.ApiResponse;
import com.vistara.tourist_tracking_system.dto.LocationUpdateDTO;
import com.vistara.tourist_tracking_system.model.LocationTracking;
import com.vistara.tourist_tracking_system.service.TrackingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tracking")
@RequiredArgsConstructor
public class TrackingController {

    private final TrackingService trackingService;

    @PostMapping("/update")
    public ResponseEntity<ApiResponse<?>> updateLocation(@Valid @RequestBody LocationUpdateDTO dto) {
        LocationTracking location = trackingService.updateLocation(dto);
        return ResponseEntity.ok(ApiResponse.success(location, "Location updated"));
    }

    @GetMapping("/last/{sessionId}")
    public ResponseEntity<ApiResponse<?>> getLastLocation(@PathVariable Long sessionId) {
        LocationTracking location = trackingService.getLastLocation(sessionId);
        return ResponseEntity.ok(ApiResponse.success(location, "Last location retrieved"));
    }
}