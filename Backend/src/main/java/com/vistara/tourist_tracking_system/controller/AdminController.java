package com.vistara.tourist_tracking_system.controller;

import com.vistara.tourist_tracking_system.dto.ApiResponse;
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
        List<VisitorSession> activeSessions = sessionRepository.findByIsActiveTrue();
        return ResponseEntity.ok(ApiResponse.success(activeSessions, "Active visitors retrieved"));
    }

    @GetMapping("/dashboard/stats")
    public ResponseEntity<ApiResponse<?>> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("activeVisitors", sessionRepository.findByIsActiveTrue().size());
        stats.put("pendingAlerts", alertRepository.findByStatus(EmergencyAlert.AlertStatus.PENDING).size());
        stats.put("totalUsers", userRepository.count());
        stats.put("todayCheckins", sessionRepository.findStaleSessions(java.time.LocalDateTime.now().minusHours(24)).size());

        return ResponseEntity.ok(ApiResponse.success(stats, "Dashboard stats retrieved"));
    }

    @PutMapping("/assign-ranger/{alertId}/{rangerId}")
    public ResponseEntity<ApiResponse<?>> assignRanger(@PathVariable Long alertId, @PathVariable Long rangerId) {
        User ranger = userRepository.findById(rangerId)
                .orElseThrow(() -> new RuntimeException("Ranger not found"));
        EmergencyAlert alert = emergencyService.assignRanger(alertId, ranger);
        return ResponseEntity.ok(ApiResponse.success(alert, "Ranger assigned successfully"));
    }

    @PutMapping("/resolve-alert/{alertId}")
    public ResponseEntity<ApiResponse<?>> resolveAlert(@PathVariable Long alertId, @RequestParam String notes) {
        EmergencyAlert alert = emergencyService.resolveAlert(alertId, notes);
        return ResponseEntity.ok(ApiResponse.success(alert, "Alert resolved"));
    }
}