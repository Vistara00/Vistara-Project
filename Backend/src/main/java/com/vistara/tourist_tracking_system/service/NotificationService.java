package com.vistara.tourist_tracking_system.service;

import com.vistara.tourist_tracking_system.dto.NotificationRequest;
import com.vistara.tourist_tracking_system.dto.NotificationResponse;
import com.vistara.tourist_tracking_system.model.Notification;
import com.vistara.tourist_tracking_system.model.User;
import com.vistara.tourist_tracking_system.model.VisitorSession;
import com.vistara.tourist_tracking_system.repository.NotificationRepository;
import com.vistara.tourist_tracking_system.repository.UserRepository;
import com.vistara.tourist_tracking_system.repository.VisitorSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final VisitorSessionRepository sessionRepository;
    private final SimpMessagingTemplate messagingTemplate;

    // Create a notification for a specific user
    @Transactional
    public Notification createNotification(User user, String title, String message, String type, Long referenceId, boolean broadcast) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setReferenceId(referenceId);
        notification.setBroadcast(broadcast);
        notification.setRead(false);

        Notification saved = notificationRepository.save(notification);
        log.info("📧 Notification created for user {}: {} - {}", user.getEmail(), type, title);

        // Send real-time notification via WebSocket
        sendWebSocketNotification(user, saved);

        return saved;
    }

    // Create notification for a user by email
    @Transactional
    public Notification createNotificationByEmail(String email, String title, String message, String type, Long referenceId, boolean broadcast) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        return createNotification(user, title, message, type, referenceId, broadcast);
    }

    // Admin broadcast notification to all users (or specific user)
    @Transactional
    public List<Notification> broadcastNotification(NotificationRequest request) {
        List<User> users;

        if (request.getUserId() != null) {
            User user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            users = List.of(user);
        } else {
            users = userRepository.findAll();
        }

        return users.stream()
                .map(user -> createNotification(user, request.getTitle(), request.getMessage(), "BROADCAST", null, true))
                .collect(Collectors.toList());
    }

    /**
     * Broadcast notification only to visitors with active sessions
     */
    @Transactional
    public List<Notification> broadcastToActiveVisitors(String title, String message) {
        List<VisitorSession> activeSessions = sessionRepository.findByActiveTrue();

        if (activeSessions.isEmpty()) {
            log.info("No active sessions found. Broadcast not sent.");
            return new ArrayList<>();
        }

        List<User> activeUsers = activeSessions.stream()
                .map(VisitorSession::getUser)
                .distinct()
                .collect(Collectors.toList());

        log.info("Sending broadcast to {} active visitors", activeUsers.size());

        return activeUsers.stream()
                .map(user -> createNotification(user, title, message, "BROADCAST", null, true))
                .collect(Collectors.toList());
    }

    /**
     * Broadcast to a specific user
     */
    @Transactional
    public Notification broadcastToUser(Long userId, String title, String message) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return createNotification(user, title, message, "BROADCAST", null, true);
    }

    /**
     * Broadcast to all users
     */
    @Transactional
    public List<Notification> broadcastToAllUsers(String title, String message) {
        List<User> allUsers = userRepository.findAll();
        return allUsers.stream()
                .map(user -> createNotification(user, title, message, "BROADCAST", null, true))
                .collect(Collectors.toList());
    }

    /**
     * Get count of active visitors (for dashboard)
     */
    public long getActiveVisitorsCount() {
        return sessionRepository.countByActiveTrue();
    }

    // ===== NOTIFICATION RETRIEVAL METHODS (USER-FACING) =====

    // Get notifications for the authenticated user (user-facing - hides sensitive fields)
    public List<NotificationResponse> getUserNotifications(User user) {
        return notificationRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(NotificationResponse::forUser)  // User sees only safe fields
                .collect(Collectors.toList());
    }

    // Get unread notifications for the authenticated user
    public List<NotificationResponse> getUnreadNotifications(User user) {
        return notificationRepository.findByUserAndReadFalseOrderByCreatedAtDesc(user)
                .stream()
                .map(NotificationResponse::forUser)
                .collect(Collectors.toList());
    }

    // Get unread count for the authenticated user
    public long getUnreadCount(User user) {
        return notificationRepository.countByUserAndReadFalse(user);
    }

    // Mark a notification as read
    @Transactional
    public NotificationResponse markAsRead(Long notificationId, User user) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (!notification.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You are not authorized to read this notification");
        }

        notification.setRead(true);
        return NotificationResponse.forUser(notificationRepository.save(notification));
    }

    // Mark all notifications as read for a user
    @Transactional
    public void markAllAsRead(User user) {
        notificationRepository.markAllAsRead(user);
    }

    // ===== ADMIN METHODS =====

    // Get all broadcast notifications (admin-facing - shows all fields)
    public List<NotificationResponse> getAllBroadcasts() {
        return notificationRepository.findBroadcastNotifications()
                .stream()
                .map(NotificationResponse::forAdmin)  // Admin sees all fields
                .collect(Collectors.toList());
    }

    // ===== WEBSOCKET HELPER =====

    private void sendWebSocketNotification(User user, Notification notification) {
        try {
            messagingTemplate.convertAndSendToUser(
                    user.getEmail(),
                    "/queue/notifications",
                    NotificationResponse.forUser(notification)
            );
            log.info("WebSocket notification sent to user: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send WebSocket notification to user {}: {}", user.getEmail(), e.getMessage());
        }
    }
}