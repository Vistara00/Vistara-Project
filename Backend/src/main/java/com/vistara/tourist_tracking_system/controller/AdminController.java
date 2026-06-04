package com.vistara.tourist_tracking_system.controller;

import com.vistara.tourist_tracking_system.dto.ApiResponse;
import com.vistara.tourist_tracking_system.exception.DuplicateResourceException;
import com.vistara.tourist_tracking_system.model.EmergencyAlert;
import com.vistara.tourist_tracking_system.model.User;
import com.vistara.tourist_tracking_system.model.VisitorSession;
import com.vistara.tourist_tracking_system.repository.EmergencyAlertRepository;
import com.vistara.tourist_tracking_system.repository.UserRepository;
import com.vistara.tourist_tracking_system.repository.VisitorSessionRepository;
import com.vistara.tourist_tracking_system.service.EmergencyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final VisitorSessionRepository sessionRepository;
    private final EmergencyAlertRepository alertRepository;
    private final UserRepository userRepository;
    private final EmergencyService emergencyService;

    @GetMapping("/active-visitors")
    public ResponseEntity<ApiResponse<?>> getActiveVisitors() {
        // FIX: was findByIsActiveTrue() — field renamed to "active" in VisitorSession,
        // so Spring Data method must be findByActiveTrue()
        List<VisitorSession> activeSessions = sessionRepository.findByActiveTrue();
        return ResponseEntity.ok(ApiResponse.success(activeSessions, "Active visitors retrieved"));
    }

    @GetMapping("/dashboard/stats")
    public ResponseEntity<ApiResponse<?>> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        // FIX: was findByIsActiveTrue() — renamed to findByActiveTrue()
        stats.put("activeVisitors", sessionRepository.findByActiveTrue().size());

        // FIX: was findByStatus() — renamed to findByAlertStatus()
        stats.put("pendingAlerts", alertRepository.findByAlertStatus(
                EmergencyAlert.AlertStatus.PENDING).size());

        stats.put("totalUsers", userRepository.count());

        // FIX: was findStaleSessions() which doesn't exist on the repository.
        // Replaced with findByActiveTrue() filtered by check-in time to get
        // today's check-ins — add the proper repository method below.
        stats.put("todayCheckins", sessionRepository
                .findByCheckInTimeAfter(LocalDateTime.now().minusHours(24)).size());

        return ResponseEntity.ok(ApiResponse.success(stats, "Dashboard stats retrieved"));
    }

    @PutMapping("/assign-ranger/{alertId}/{rangerId}")
    public ResponseEntity<ApiResponse<?>> assignRanger(
            @PathVariable Long alertId,
            @PathVariable Long rangerId) {

        // FIX: was RuntimeException — use DuplicateResourceException for consistency
        User ranger = userRepository.findById(rangerId)
                .orElseThrow(() -> new DuplicateResourceException("Ranger not found"));

        EmergencyAlert alert = emergencyService.assignRanger(alertId, ranger);
        return ResponseEntity.ok(ApiResponse.success(alert, "Ranger assigned successfully"));
    }

    @PutMapping("/resolve-alert/{alertId}")
    public ResponseEntity<ApiResponse<?>> resolveAlert(
            @PathVariable Long alertId,
            @RequestParam String notes) {

        EmergencyAlert alert = emergencyService.resolveAlert(alertId, notes);
        return ResponseEntity.ok(ApiResponse.success(alert, "Alert resolved"));
    }
}