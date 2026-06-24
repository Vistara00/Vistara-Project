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
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

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
                .map(notification -> {
                    NotificationResponse resp = new NotificationResponse();
                    resp.setId(notification.getId());
                    resp.setTitle(notification.getTitle());
                    resp.setMessage(notification.getMessage());
                    resp.setType(notification.getType());
                    resp.setBroadcast(notification.isBroadcast());
                    resp.setRead(notification.isRead());
                    resp.setReferenceId(notification.getReferenceId());
                    resp.setCreatedAt(notification.getCreatedAt());
                    return resp;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(responses, "Broadcast sent successfully"));
    }

    @GetMapping("/broadcasts")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getMyBroadcasts(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByEmail(userDetails.getUsername());
        List<NotificationResponse> broadcasts = notificationService.getUserNotifications(user)
                .stream()
                .filter(n -> n.isBroadcast())
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(broadcasts, "Your broadcasts retrieved"));
    }
}