package com.vistara.tourist_tracking_system.controller;

import com.vistara.tourist_tracking_system.dto.ApiResponse;
import com.vistara.tourist_tracking_system.dto.EmergencyAlertResponse;
import com.vistara.tourist_tracking_system.dto.NotificationResponse;
import com.vistara.tourist_tracking_system.dto.ResolveAlertRequest;
import com.vistara.tourist_tracking_system.model.EmergencyAlert;
import com.vistara.tourist_tracking_system.model.User;
import com.vistara.tourist_tracking_system.repository.EmergencyAlertRepository;
import com.vistara.tourist_tracking_system.repository.VisitorSessionRepository;
import com.vistara.tourist_tracking_system.service.EmergencyService;
import com.vistara.tourist_tracking_system.service.NotificationService;
import com.vistara.tourist_tracking_system.service.UserService;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/ranger")
@RequiredArgsConstructor
public class RangerController {

    private final EmergencyService emergencyService;
    private final UserService userService;
    private final EmergencyAlertRepository alertRepository;
    private final VisitorSessionRepository sessionRepository;
    private final NotificationService notificationService;

    // ===== ALERT ENDPOINTS =====

    @GetMapping("/alerts/all")
    public ResponseEntity<ApiResponse<List<EmergencyAlertResponse>>> getAllAlerts(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        User ranger = userService.findByEmail(userDetails.getUsername());
        List<EmergencyAlertResponse> alerts = emergencyService.getAllAlerts();
        log.info("Ranger {} retrieved all alerts. Total: {}", ranger.getEmail(), alerts.size());
        return ResponseEntity.ok(ApiResponse.success(alerts, "All alerts retrieved successfully"));
    }

    @GetMapping("/alerts/assigned")
    public ResponseEntity<ApiResponse<List<EmergencyAlertResponse>>> getAssignedAlerts(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        User ranger = userService.findByEmail(userDetails.getUsername());
        List<EmergencyAlertResponse> alerts = emergencyService.getAlertsByRanger(ranger);
        log.info("Ranger {} retrieved {} assigned alerts", ranger.getEmail(), alerts.size());
        return ResponseEntity.ok(ApiResponse.success(alerts, "Assigned alerts retrieved successfully"));
    }

    @GetMapping("/alerts/assigned/status/{status}")
    public ResponseEntity<ApiResponse<List<EmergencyAlertResponse>>> getAssignedAlertsByStatus(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable EmergencyAlert.AlertStatus status) {
        User ranger = userService.findByEmail(userDetails.getUsername());
        List<EmergencyAlertResponse> alerts = emergencyService.getAlertsByRangerAndStatus(ranger, status);
        log.info("Ranger {} retrieved {} assigned alerts with status {}",
                ranger.getEmail(), alerts.size(), status);
        return ResponseEntity.ok(ApiResponse.success(alerts, "Assigned alerts by status retrieved successfully"));
    }

    @GetMapping("/alerts/pending")
    public ResponseEntity<ApiResponse<List<EmergencyAlertResponse>>> getPendingAlerts(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        User ranger = userService.findByEmail(userDetails.getUsername());
        List<EmergencyAlertResponse> alerts = emergencyService.getPendingAlerts();
        log.info("Ranger {} retrieved {} pending alerts", ranger.getEmail(), alerts.size());
        return ResponseEntity.ok(ApiResponse.success(alerts, "Pending alerts retrieved successfully"));
    }

    @GetMapping("/alerts/status/{status}")
    public ResponseEntity<ApiResponse<List<EmergencyAlertResponse>>> getAlertsByStatus(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable EmergencyAlert.AlertStatus status) {
        User ranger = userService.findByEmail(userDetails.getUsername());
        List<EmergencyAlertResponse> alerts = emergencyService.getAlertsByStatus(status);
        log.info("Ranger {} retrieved {} alerts with status {}", ranger.getEmail(), alerts.size(), status);
        return ResponseEntity.ok(ApiResponse.success(alerts, "Alerts by status retrieved successfully"));
    }

    @GetMapping("/alerts/{alertId}")
    public ResponseEntity<ApiResponse<EmergencyAlertResponse>> getAlertById(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long alertId) {
        User ranger = userService.findByEmail(userDetails.getUsername());
        EmergencyAlertResponse alert = emergencyService.getAlertById(alertId);
        log.info("Ranger {} viewed alert {}", ranger.getEmail(), alertId);
        return ResponseEntity.ok(ApiResponse.success(alert, "Alert retrieved successfully"));
    }

    @GetMapping("/alerts/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAlertStats(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        User ranger = userService.findByEmail(userDetails.getUsername());

        List<EmergencyAlertResponse> assigned = emergencyService.getAlertsByRanger(ranger);
        List<EmergencyAlertResponse> pending = emergencyService.getPendingAlerts();

        long totalAssigned = assigned.size();
        long assignedPending = assigned.stream()
                .filter(a -> "PENDING".equals(a.getAlertStatus()))
                .count();
        long assignedResponding = assigned.stream()
                .filter(a -> "RESPONDING".equals(a.getAlertStatus()))
                .count();
        long assignedResolved = assigned.stream()
                .filter(a -> "RESOLVED".equals(a.getAlertStatus()))
                .count();
        long assignedFalseAlarm = assigned.stream()
                .filter(a -> "FALSE_ALARM".equals(a.getAlertStatus()))
                .count();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalAssigned", totalAssigned);
        stats.put("assignedPending", assignedPending);
        stats.put("assignedResponding", assignedResponding);
        stats.put("assignedResolved", assignedResolved);
        stats.put("assignedFalseAlarm", assignedFalseAlarm);
        stats.put("totalPendingAlerts", pending.size());

        return ResponseEntity.ok(ApiResponse.success(stats, "Alert statistics retrieved"));
    }

    @PostMapping("/alerts/{alertId}/claim")
    public ResponseEntity<ApiResponse<EmergencyAlertResponse>> claimAlert(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long alertId) {

        User ranger = userService.findByEmail(userDetails.getUsername());

        try {
            EmergencyAlertResponse alert = emergencyService.claimAlert(alertId, ranger);
            log.info("Ranger {} claimed alert {}", ranger.getEmail(), alertId);
            return ResponseEntity.ok(ApiResponse.success(alert, "Alert claimed successfully. Status changed to RESPONDING."));
        } catch (RuntimeException e) {
            log.warn("Ranger {} failed to claim alert {}: {}", ranger.getEmail(), alertId, e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/alerts/{alertId}/resolve")
    public ResponseEntity<ApiResponse<EmergencyAlertResponse>> resolveAlertByRanger(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long alertId,
            @Valid @RequestBody ResolveAlertRequest request) {

        User ranger = userService.findByEmail(userDetails.getUsername());

        try {
            EmergencyAlertResponse alert = emergencyService.resolveAlertByRanger(
                    alertId,
                    request.getNotes(),
                    ranger
            );
            log.info("Ranger {} resolved alert {}", ranger.getEmail(), alertId);
            return ResponseEntity.ok(ApiResponse.success(alert, "Alert resolved successfully. Status changed to RESOLVED."));
        } catch (RuntimeException e) {
            log.warn("Ranger {} failed to resolve alert {}: {}", ranger.getEmail(), alertId, e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/alerts/active")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getActiveAlertStatus(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {

        User ranger = userService.findByEmail(userDetails.getUsername());
        boolean hasActive = emergencyService.hasActiveAlert(ranger);
        EmergencyAlertResponse activeAlert = emergencyService.getCurrentActiveAlert(ranger);

        Map<String, Object> response = new HashMap<>();
        response.put("hasActiveAlert", hasActive);
        response.put("activeAlert", activeAlert);

        return ResponseEntity.ok(ApiResponse.success(response, "Active alert status retrieved"));
    }

    @GetMapping("/alerts/active-alert")
    public ResponseEntity<ApiResponse<EmergencyAlertResponse>> getCurrentActiveAlert(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {

        User ranger = userService.findByEmail(userDetails.getUsername());
        EmergencyAlertResponse alert = emergencyService.getCurrentActiveAlert(ranger);

        if (alert == null) {
            return ResponseEntity.ok(ApiResponse.success(null, "No active alert found"));
        }

        return ResponseEntity.ok(ApiResponse.success(alert, "Active alert retrieved"));
    }

    // ===== NOTIFICATION ENDPOINTS FOR RANGERS =====

    /**
     * ✅ Get all notifications for the authenticated ranger
     * Shows all notifications including alert assignments, status changes, etc.
     */
    @GetMapping("/notifications")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getRangerNotifications(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        User ranger = userService.findByEmail(userDetails.getUsername());
        List<NotificationResponse> notifications = notificationService.getUserNotifications(ranger);
        log.info("Ranger {} retrieved {} notifications", ranger.getEmail(), notifications.size());
        return ResponseEntity.ok(ApiResponse.success(notifications, "Notifications retrieved successfully"));
    }

    /**
     * ✅ Get unread notifications for the authenticated ranger
     */
    @GetMapping("/notifications/unread")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getUnreadNotifications(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        User ranger = userService.findByEmail(userDetails.getUsername());
        List<NotificationResponse> notifications = notificationService.getUnreadNotifications(ranger);
        log.info("Ranger {} retrieved {} unread notifications", ranger.getEmail(), notifications.size());
        return ResponseEntity.ok(ApiResponse.success(notifications, "Unread notifications retrieved successfully"));
    }

    /**
     * ✅ Get unread notification count for the authenticated ranger
     */
    @GetMapping("/notifications/unread-count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        User ranger = userService.findByEmail(userDetails.getUsername());
        long count = notificationService.getUnreadCount(ranger);
        log.info("Ranger {} has {} unread notifications", ranger.getEmail(), count);
        return ResponseEntity.ok(ApiResponse.success(count, "Unread count retrieved"));
    }

    /**
     * ✅ Get alert-type notifications only for the ranger
     * Shows all notifications related to alerts (new, assigned, responding, resolved, false alarm)
     */
    @GetMapping("/notifications/alerts")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getAlertNotifications(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        User ranger = userService.findByEmail(userDetails.getUsername());

        List<NotificationResponse> alertNotifications = notificationService.getUserNotifications(ranger)
                .stream()
                .filter(n -> n.getType() != null && n.getType().startsWith("ALERT"))
                .collect(Collectors.toList());

        log.info("Ranger {} retrieved {} alert notifications", ranger.getEmail(), alertNotifications.size());
        return ResponseEntity.ok(ApiResponse.success(alertNotifications, "Alert notifications retrieved successfully"));
    }

    /**
     * ✅ Get notifications grouped by type for the ranger
     * Returns counts by notification type (ALERT_NEW, ALERT_ASSIGNED, ALERT_RESPONDING, etc.)
     */
    @GetMapping("/notifications/stats")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getNotificationStats(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        User ranger = userService.findByEmail(userDetails.getUsername());

        List<NotificationResponse> notifications = notificationService.getUserNotifications(ranger);

        Map<String, Long> stats = notifications.stream()
                .collect(Collectors.groupingBy(
                        n -> n.getType() != null ? n.getType() : "OTHER",
                        Collectors.counting()
                ));

        log.info("Ranger {} retrieved notification stats", ranger.getEmail());
        return ResponseEntity.ok(ApiResponse.success(stats, "Notification statistics retrieved"));
    }

    /**
     * ✅ Mark a notification as read
     */
    @PutMapping("/notifications/{notificationId}/read")
    public ResponseEntity<ApiResponse<NotificationResponse>> markNotificationAsRead(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long notificationId) {
        User ranger = userService.findByEmail(userDetails.getUsername());
        NotificationResponse notification = notificationService.markAsRead(notificationId, ranger);
        log.info("Ranger {} marked notification {} as read", ranger.getEmail(), notificationId);
        return ResponseEntity.ok(ApiResponse.success(notification, "Notification marked as read"));
    }

    /**
     * ✅ Mark all notifications as read for the ranger
     */
    @PutMapping("/notifications/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        User ranger = userService.findByEmail(userDetails.getUsername());
        notificationService.markAllAsRead(ranger);
        log.info("Ranger {} marked all notifications as read", ranger.getEmail());
        return ResponseEntity.ok(ApiResponse.success(null, "All notifications marked as read"));
    }

    /**
     * ✅ Get recent notifications (last 24 hours)
     */
    @GetMapping("/notifications/recent")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getRecentNotifications(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        User ranger = userService.findByEmail(userDetails.getUsername());

        List<NotificationResponse> notifications = notificationService.getUserNotifications(ranger)
                .stream()
                .filter(n -> n.getCreatedAt() != null &&
                        n.getCreatedAt().isAfter(java.time.LocalDateTime.now().minusHours(24)))
                .collect(Collectors.toList());

        log.info("Ranger {} retrieved {} recent notifications", ranger.getEmail(), notifications.size());
        return ResponseEntity.ok(ApiResponse.success(notifications, "Recent notifications retrieved successfully"));
    }
}