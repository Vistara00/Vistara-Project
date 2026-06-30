package com.vistara.tourist_tracking_system.controller;

import com.vistara.tourist_tracking_system.dto.ApiResponse;
import com.vistara.tourist_tracking_system.dto.NotificationRequest;
import com.vistara.tourist_tracking_system.dto.NotificationResponse;
import com.vistara.tourist_tracking_system.model.Notification;
import com.vistara.tourist_tracking_system.model.User;
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

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final UserService userService;

    // ===== USER ENDPOINTS =====

    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getMyNotifications(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByEmail(userDetails.getUsername());
        List<NotificationResponse> notifications = notificationService.getUserNotifications(user);
        return ResponseEntity.ok(ApiResponse.success(notifications, "Notifications retrieved"));
    }

    @GetMapping("/unread")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getUnreadNotifications(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByEmail(userDetails.getUsername());
        List<NotificationResponse> notifications = notificationService.getUnreadNotifications(user);
        return ResponseEntity.ok(ApiResponse.success(notifications, "Unread notifications retrieved"));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByEmail(userDetails.getUsername());
        long count = notificationService.getUnreadCount(user);
        return ResponseEntity.ok(ApiResponse.success(count, "Unread count retrieved"));
    }

    @PutMapping("/{notificationId}/read")
    public ResponseEntity<ApiResponse<NotificationResponse>> markAsRead(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long notificationId) {
        User user = userService.findByEmail(userDetails.getUsername());
        NotificationResponse notification = notificationService.markAsRead(notificationId, user);
        return ResponseEntity.ok(ApiResponse.success(notification, "Notification marked as read"));
    }

    @PutMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByEmail(userDetails.getUsername());
        notificationService.markAllAsRead(user);
        return ResponseEntity.ok(ApiResponse.success(null, "All notifications marked as read"));
    }

    // ===== ADMIN ENDPOINTS =====

    @PostMapping("/admin/broadcast")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> broadcastNotification(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails adminDetails,
            @Valid @RequestBody NotificationRequest request) {

        List<Notification> notifications = notificationService.broadcastNotification(request);

        List<NotificationResponse> responses = notifications.stream()
                .map(NotificationResponse::forAdmin)
                .collect(Collectors.toList());

        log.info("Admin {} broadcasted notification: {}", adminDetails.getUsername(), request.getTitle());
        return ResponseEntity.ok(ApiResponse.success(responses, "Broadcast sent successfully"));
    }

    @GetMapping("/broadcasts")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getMyBroadcasts(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByEmail(userDetails.getUsername());

        List<NotificationResponse> broadcasts = notificationService.getUserNotifications(user)
                .stream()
                .filter(NotificationResponse::isBroadcast)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(broadcasts, "Your broadcasts retrieved"));
    }

    /**
     * Get all ALERT type notifications (for the authenticated user)
     * Shows all SOS alerts and emergency notifications
     */
    @GetMapping("/alerts")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getMyAlerts(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByEmail(userDetails.getUsername());

        List<NotificationResponse> alerts = notificationService.getUserNotifications(user)
                .stream()
                .filter(n -> "ALERT".equals(n.getType()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(alerts, "Your alerts retrieved"));
    }

    /**
     * Get unread ALERT type notifications count (for badge)
     */
    @GetMapping("/alerts/unread-count")
    public ResponseEntity<ApiResponse<Long>> getUnreadAlertCount(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByEmail(userDetails.getUsername());

        long count = notificationService.getUserNotifications(user)
                .stream()
                .filter(n -> "ALERT".equals(n.getType()) && !n.isRead())
                .count();

        return ResponseEntity.ok(ApiResponse.success(count, "Unread alerts count retrieved"));
    }
}