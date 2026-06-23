package com.vistara.tourist_tracking_system.service;

import com.vistara.tourist_tracking_system.dto.NotificationRequest;
import com.vistara.tourist_tracking_system.dto.NotificationResponse;
import com.vistara.tourist_tracking_system.model.Notification;
import com.vistara.tourist_tracking_system.model.User;
import com.vistara.tourist_tracking_system.repository.NotificationRepository;
import com.vistara.tourist_tracking_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
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
            // Send to specific user
            User user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            users = List.of(user);
        } else {
            // Broadcast to all users
            users = userRepository.findAll();
        }

        return users.stream()
                .map(user -> createNotification(user, request.getTitle(), request.getMessage(), "BROADCAST", null, true))
                .collect(Collectors.toList());
    }

    // Get notifications for the authenticated user
    public List<NotificationResponse> getUserNotifications(User user) {
        return notificationRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // Get unread notifications for the authenticated user
    public List<NotificationResponse> getUnreadNotifications(User user) {
        return notificationRepository.findByUserAndReadFalseOrderByCreatedAtDesc(user)
                .stream()
                .map(this::convertToResponse)
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
        return convertToResponse(notificationRepository.save(notification));
    }

    // Mark all notifications as read for a user
    @Transactional
    public void markAllAsRead(User user) {
        notificationRepository.markAllAsRead(user);
    }

    // WebSocket notification helper
    private void sendWebSocketNotification(User user, Notification notification) {
        try {
            messagingTemplate.convertAndSendToUser(
                    user.getEmail(),
                    "/queue/notifications",
                    convertToResponse(notification)
            );
            log.info("WebSocket notification sent to user: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send WebSocket notification to user {}: {}", user.getEmail(), e.getMessage());
        }
    }

    private NotificationResponse convertToResponse(Notification notification) {
        NotificationResponse response = new NotificationResponse();
        response.setId(notification.getId());
        response.setTitle(notification.getTitle());
        response.setMessage(notification.getMessage());
        response.setType(notification.getType());
        response.setRead(notification.isRead());
        response.setBroadcast(notification.isBroadcast());
        response.setReferenceId(notification.getReferenceId());
        response.setCreatedAt(notification.getCreatedAt());
        return response;
    }
}