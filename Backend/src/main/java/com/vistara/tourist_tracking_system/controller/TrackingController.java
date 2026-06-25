package com.vistara.tourist_tracking_system.controller;

import com.vistara.tourist_tracking_system.dto.ApiResponse;
import com.vistara.tourist_tracking_system.dto.LocationTrackingResponse;
import com.vistara.tourist_tracking_system.dto.LocationUpdateDTO;
import com.vistara.tourist_tracking_system.model.LocationTracking;
import com.vistara.tourist_tracking_system.service.TrackingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/tracking")
@RequiredArgsConstructor
public class TrackingController {

    private final TrackingService trackingService;

    @PostMapping("/update")
    public ResponseEntity<ApiResponse<LocationTrackingResponse>> updateLocation(
            @Valid @RequestBody LocationUpdateDTO dto) {
        LocationTracking location = trackingService.updateLocation(dto);
        LocationTrackingResponse response = convertToResponse(location);
        return ResponseEntity.ok(ApiResponse.success(response, "Location updated"));
    }

    @GetMapping("/last/{sessionId}")
    public ResponseEntity<ApiResponse<LocationTrackingResponse>> getLastLocation(
            @PathVariable Long sessionId) {
        LocationTracking location = trackingService.getLastLocation(sessionId);
        LocationTrackingResponse response = convertToResponse(location);
        return ResponseEntity.ok(ApiResponse.success(response, "Last location retrieved"));
    }

    // ✅ ADD THIS METHOD
    @GetMapping("/history/{sessionId}")
    public ResponseEntity<ApiResponse<List<LocationTrackingResponse>>> getLocationHistory(
            @PathVariable Long sessionId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {

        // Default to last 24 hours if dates not provided
        if (from == null) {
            from = LocalDateTime.now().minusHours(24);
        }
        if (to == null) {
            to = LocalDateTime.now();
        }

        List<LocationTracking> locations = trackingService.getLocationHistory(sessionId, from, to);
        List<LocationTrackingResponse> responses = locations.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(responses, "Location history retrieved"));
    }

    // Helper method to convert entity to response DTO
    private LocationTrackingResponse convertToResponse(LocationTracking location) {
        LocationTrackingResponse response = new LocationTrackingResponse();
        response.setId(location.getId());
        response.setSessionId(location.getSession().getId());
        response.setLatitude(location.getLatitude());
        response.setLongitude(location.getLongitude());
        response.setAccuracy(location.getAccuracy());
        response.setBatteryLevel(location.getBatteryLevel());
        response.setWithinGeofence(location.isWithinGeofence());
        response.setTimestamp(location.getTimestamp());
        response.setCreatedAt(location.getCreatedAt());
        return response;
    }
}