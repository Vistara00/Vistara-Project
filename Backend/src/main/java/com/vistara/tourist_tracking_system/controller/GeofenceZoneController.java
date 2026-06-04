package com.vistara.tourist_tracking_system.controller;

import com.vistara.tourist_tracking_system.dto.ApiResponse;
import com.vistara.tourist_tracking_system.dto.GeofenceZoneRequest;
import com.vistara.tourist_tracking_system.dto.GeofenceZoneResponse;
import com.vistara.tourist_tracking_system.model.GeofenceZone;
import com.vistara.tourist_tracking_system.service.GeofenceZoneService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/geofence")
@RequiredArgsConstructor
public class GeofenceZoneController {

    private final GeofenceZoneService geofenceZoneService;

    @PostMapping("/admin/zones")
    public ResponseEntity<ApiResponse<GeofenceZoneResponse>> createZone(
            @Valid @RequestBody GeofenceZoneRequest request,
            @AuthenticationPrincipal UserDetails admin) {
        Long adminId = extractUserId(admin);
        GeofenceZoneResponse response = geofenceZoneService.createZone(request, adminId);
        return ResponseEntity.ok(ApiResponse.success(response, "Geofence zone created successfully"));
    }

    @GetMapping("/zones")
    public ResponseEntity<ApiResponse<List<GeofenceZoneResponse>>> getAllActiveZones() {
        List<GeofenceZoneResponse> zones = geofenceZoneService.getAllActiveZones();
        return ResponseEntity.ok(ApiResponse.success(zones, "Active zones retrieved"));
    }

    @GetMapping("/zones/{id}")
    public ResponseEntity<ApiResponse<GeofenceZoneResponse>> getZoneById(@PathVariable Long id) {
        GeofenceZoneResponse zone = geofenceZoneService.getZoneById(id);
        return ResponseEntity.ok(ApiResponse.success(zone, "Zone retrieved"));
    }

    @PutMapping("/admin/zones/{id}")
    public ResponseEntity<ApiResponse<GeofenceZoneResponse>> updateZone(
            @PathVariable Long id,
            @Valid @RequestBody GeofenceZoneRequest request,
            @AuthenticationPrincipal UserDetails admin) {
        Long adminId = extractUserId(admin);
        GeofenceZoneResponse response = geofenceZoneService.updateZone(id, request, adminId);
        return ResponseEntity.ok(ApiResponse.success(response, "Zone updated successfully"));
    }

    @DeleteMapping("/admin/zones/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteZone(@PathVariable Long id) {
        geofenceZoneService.deleteZone(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Zone deactivated successfully"));
    }

    @GetMapping("/zones/type/{type}")
    public ResponseEntity<ApiResponse<List<GeofenceZoneResponse>>> getZonesByType(
            @PathVariable GeofenceZone.ZoneType type) {
        List<GeofenceZoneResponse> zones = geofenceZoneService.getZonesByType(type);
        return ResponseEntity.ok(ApiResponse.success(zones, "Zones by type retrieved"));
    }

    @GetMapping("/check-location")
    public ResponseEntity<ApiResponse<GeofenceZoneResponse>> checkVisitorLocation(
            @RequestParam double latitude,
            @RequestParam double longitude) {
        GeofenceZoneResponse zone = geofenceZoneService.checkVisitorLocation(latitude, longitude);
        if (zone == null) {
            return ResponseEntity.ok(ApiResponse.success(null, "Visitor is not in any special zone"));
        }
        return ResponseEntity.ok(ApiResponse.success(zone, "Zone found at location"));
    }

    private Long extractUserId(UserDetails userDetails) {
        // Implement based on your User model
        // You can fetch from database or JWT claims
        return 1L; // Placeholder - replace with actual logic
    }
}