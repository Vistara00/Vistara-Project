package com.vistara.tourist_tracking_system.controller;

import com.vistara.tourist_tracking_system.dto.ApiResponse;
import com.vistara.tourist_tracking_system.dto.GeofenceZoneRequest;
import com.vistara.tourist_tracking_system.dto.GeofenceZoneResponse;
import com.vistara.tourist_tracking_system.model.GeofenceZone;
import com.vistara.tourist_tracking_system.model.User;
import com.vistara.tourist_tracking_system.service.GeofenceZoneService;
import com.vistara.tourist_tracking_system.service.UserService;
import io.swagger.v3.oas.annotations.Parameter;
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
    private final UserService userService;

    // ========== ADMIN ENDPOINTS ==========

    @PostMapping("/admin/zones")
    public ResponseEntity<ApiResponse<GeofenceZoneResponse>> createZone(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails adminDetails,
            @Valid @RequestBody GeofenceZoneRequest request) {
        User admin = extractUser(adminDetails);
        GeofenceZoneResponse response = geofenceZoneService.createZone(request, admin.getId());
        return ResponseEntity.ok(ApiResponse.success(response, "Geofence zone created successfully"));
    }

    @PutMapping("/admin/zones/{id}")
    public ResponseEntity<ApiResponse<GeofenceZoneResponse>> updateZone(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails adminDetails,
            @PathVariable Long id,
            @Valid @RequestBody GeofenceZoneRequest request) {
        User admin = extractUser(adminDetails);
        GeofenceZoneResponse response = geofenceZoneService.updateZone(id, request, admin.getId());
        return ResponseEntity.ok(ApiResponse.success(response, "Zone updated successfully"));
    }

    @DeleteMapping("/admin/zones/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteZone(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails adminDetails,
            @PathVariable Long id) {
        User admin = extractUser(adminDetails);
        geofenceZoneService.deleteZone(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Zone deactivated successfully"));
    }

    // ========== PUBLIC ENDPOINTS ==========

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

    // ========== NEW ENDPOINTS ==========

    /**
     * Get all geofence zones (including inactive) - Admin only
     */
    @GetMapping("/admin/all-zones")
    public ResponseEntity<ApiResponse<List<GeofenceZoneResponse>>> getAllZones(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails adminDetails) {
        User admin = extractUser(adminDetails);
        List<GeofenceZoneResponse> zones = geofenceZoneService.getAllZones();
        return ResponseEntity.ok(ApiResponse.success(zones, "All zones retrieved"));
    }

    /**
     * Get zones with visitor count
     */
    @GetMapping("/zones/with-count")
    public ResponseEntity<ApiResponse<List<GeofenceZoneResponse>>> getZonesWithVisitorCount() {
        List<GeofenceZoneResponse> zones = geofenceZoneService.getZonesWithVisitorCount();
        return ResponseEntity.ok(ApiResponse.success(zones, "Zones with visitor count retrieved"));
    }

    /**
     * Get zones near a location (within radius)
     */
    @GetMapping("/zones/near")
    public ResponseEntity<ApiResponse<List<GeofenceZoneResponse>>> getZonesNearLocation(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam(defaultValue = "5000") double radiusMeters) {
        List<GeofenceZoneResponse> zones = geofenceZoneService.getZonesNearLocation(latitude, longitude, radiusMeters);
        return ResponseEntity.ok(ApiResponse.success(zones, "Zones near location retrieved"));
    }

    /**
     * Toggle zone active status
     */
    @PatchMapping("/admin/zones/{id}/toggle")
    public ResponseEntity<ApiResponse<GeofenceZoneResponse>> toggleZoneStatus(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails adminDetails,
            @PathVariable Long id) {
        User admin = extractUser(adminDetails);
        GeofenceZoneResponse response = geofenceZoneService.toggleZoneStatus(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Zone status toggled successfully"));
    }

    /**
     * Get geofence zone statistics
     */
    @GetMapping("/admin/stats")
    public ResponseEntity<ApiResponse<GeofenceZoneService.ZoneStats>> getZoneStats(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails adminDetails) {
        User admin = extractUser(adminDetails);
        GeofenceZoneService.ZoneStats stats = geofenceZoneService.getZoneStats();
        return ResponseEntity.ok(ApiResponse.success(stats, "Zone statistics retrieved"));
    }

    // ========== PRIVATE HELPERS ==========

    private User extractUser(UserDetails userDetails) {
        if (userDetails == null) {
            throw new RuntimeException("User not authenticated");
        }
        return userService.findByEmail(userDetails.getUsername());
    }
}