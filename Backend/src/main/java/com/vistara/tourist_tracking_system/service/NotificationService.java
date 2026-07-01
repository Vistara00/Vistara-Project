package com.vistara.tourist_tracking_system.service;

import com.vistara.tourist_tracking_system.dto.NotificationRequest;
import com.vistara.tourist_tracking_system.dto.NotificationResponse;
import com.vistara.tourist_tracking_system.model.EmergencyAlert;
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

    // ===== CORE NOTIFICATION CREATION =====

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

    /**
     * ✅ Create notification by email address
     * Finds the user by email and creates a notification for them
     */
    @Transactional
    public Notification createNotificationByEmail(String email, String title, String message, String type, Long referenceId, boolean broadcast) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        return createNotification(user, title, message, type, referenceId, broadcast);
    }

    // ===== RANGER NOTIFICATIONS FOR ALERTS =====

    /**
     * ✅ Notify all rangers about a new pending alert
     */
    @Transactional
    public List<Notification> notifyRangersAboutNewAlert(EmergencyAlert alert) {
        List<User> rangers = userRepository.findByRole(User.Role.RANGER);

        if (rangers.isEmpty()) {
            log.warn("No rangers found to notify about alert {}", alert.getId());
            return new ArrayList<>();
        }

        String title = "🚨 NEW EMERGENCY ALERT";
        String message = String.format(
                "Alert #%d | Type: %s | Priority: %s | Location: %.6f, %.6f\nPlease check the alert and respond.",
                alert.getId(),
                alert.getAlertType().name(),
                alert.getPriority().name(),
                alert.getLatitude(),
                alert.getLongitude()
        );

        List<Notification> notifications = new ArrayList<>();
        for (User ranger : rangers) {
            Notification notification = createNotification(
                    ranger,
                    title,
                    message,
                    "ALERT_NEW",
                    alert.getId(),
                    true
            );
            notifications.add(notification);
        }

        log.info("✅ Notified {} rangers about new alert {}", notifications.size(), alert.getId());
        return notifications;
    }

    /**
     * ✅ Notify a specific ranger about an alert assignment
     */
    @Transactional
    public Notification notifyRangerAssigned(User ranger, EmergencyAlert alert) {
        String title = "🚨 ALERT ASSIGNED TO YOU";
        String message = String.format(
                "Alert #%d | Type: %s | Priority: %s\nLocation: %.6f, %.6f\n\nPlease respond immediately!",
                alert.getId(),
                alert.getAlertType().name(),
                alert.getPriority().name(),
                alert.getLatitude(),
                alert.getLongitude()
        );

        Notification notification = createNotification(
                ranger,
                title,
                message,
                "ALERT_ASSIGNED",
                alert.getId(),
                true
        );

        log.info("✅ Ranger {} notified about assignment to alert {}", ranger.getEmail(), alert.getId());
        return notification;
    }

    /**
     * ✅ Notify ranger about response started
     */
    @Transactional
    public Notification notifyRangerResponding(User ranger, EmergencyAlert alert) {
        String title = "✅ RESPONSE STARTED";
        String message = String.format(
                "You have started responding to alert #%d\nType: %s | Priority: %s\nLocation: %.6f, %.6f\n\nProceed to the location immediately.",
                alert.getId(),
                alert.getAlertType().name(),
                alert.getPriority().name(),
                alert.getLatitude(),
                alert.getLongitude()
        );

        Notification notification = createNotification(
                ranger,
                title,
                message,
                "ALERT_RESPONDING",
                alert.getId(),
                true
        );

        log.info("✅ Ranger {} notified about responding to alert {}", ranger.getEmail(), alert.getId());
        return notification;
    }

    /**
     * ✅ Notify ranger about alert resolution
     */
    @Transactional
    public Notification notifyRangerResolved(User ranger, EmergencyAlert alert, String notes) {
        String title = "✅ ALERT RESOLVED";
        String message = String.format(
                "Alert #%d has been resolved.\nType: %s\nResolution Notes: %s\n\nGreat job!",
                alert.getId(),
                alert.getAlertType().name(),
                notes != null ? notes : "No additional notes"
        );

        Notification notification = createNotification(
                ranger,
                title,
                message,
                "ALERT_RESOLVED",
                alert.getId(),
                true
        );

        log.info("✅ Ranger {} notified about resolution of alert {}", ranger.getEmail(), alert.getId());
        return notification;
    }

    /**
     * ✅ Notify ranger about false alarm
     */
    @Transactional
    public Notification notifyRangerFalseAlarm(User ranger, EmergencyAlert alert, String notes) {
        String title = "🚨 FALSE ALARM";
        String message = String.format(
                "Alert #%d has been marked as a FALSE ALARM.\nType: %s\nNotes: %s",
                alert.getId(),
                alert.getAlertType().name(),
                notes != null ? notes : "No additional notes"
        );

        Notification notification = createNotification(
                ranger,
                title,
                message,
                "ALERT_FALSE_ALARM",
                alert.getId(),
                true
        );

        log.info("✅ Ranger {} notified about false alarm for alert {}", ranger.getEmail(), alert.getId());
        return notification;
    }

    /**
     * ✅ Notify ranger about unassignment
     */
    @Transactional
    public Notification notifyRangerUnassigned(User ranger, EmergencyAlert alert, String reason) {
        String title = "⚠️ ALERT UNASSIGNED";
        String message = String.format(
                "You have been unassigned from alert #%d\nReason: %s",
                alert.getId(),
                reason != null ? reason : "No reason provided"
        );

        Notification notification = createNotification(
                ranger,
                title,
                message,
                "ALERT_UNASSIGNED",
                alert.getId(),
                true
        );

        log.info("✅ Ranger {} notified about unassignment from alert {}", ranger.getEmail(), alert.getId());
        return notification;
    }

    /**
     * ✅ Notify ranger about alert status change
     */
    @Transactional
    public Notification notifyRangerStatusChange(User ranger, EmergencyAlert alert, String oldStatus, String newStatus) {
        String title = "🔄 ALERT STATUS UPDATED";
        String message = String.format(
                "Alert #%d status has changed from %s to %s\nType: %s | Priority: %s",
                alert.getId(),
                oldStatus,
                newStatus,
                alert.getAlertType().name(),
                alert.getPriority().name()
        );

        Notification notification = createNotification(
                ranger,
                title,
                message,
                "ALERT_STATUS_CHANGE",
                alert.getId(),
                true
        );

        log.info("✅ Ranger {} notified about status change for alert {}", ranger.getEmail(), alert.getId());
        return notification;
    }

    // ===== ADMIN NOTIFICATIONS =====

    /**
     * ✅ Notify all admins about new alert
     */
    @Transactional
    public List<Notification> notifyAdminsAboutNewAlert(EmergencyAlert alert) {
        List<User> admins = userRepository.findByRole(User.Role.ADMIN);

        if (admins.isEmpty()) {
            log.warn("No admins found to notify about alert {}", alert.getId());
            return new ArrayList<>();
        }

        String title = "🚨 NEW SOS ALERT RECEIVED";
        String message = String.format(
                "Alert #%d | Type: %s | Priority: %s\nLocation: %.6f, %.6f\nPlease assign a ranger.",
                alert.getId(),
                alert.getAlertType().name(),
                alert.getPriority().name(),
                alert.getLatitude(),
                alert.getLongitude()
        );

        List<Notification> notifications = new ArrayList<>();
        for (User admin : admins) {
            Notification notification = createNotification(
                    admin,
                    title,
                    message,
                    "ALERT_NEW",
                    alert.getId(),
                    true
            );
            notifications.add(notification);
        }

        log.info("✅ Notified {} admins about new alert {}", notifications.size(), alert.getId());
        return notifications;
    }

    /**
     * ✅ Notify admin about ranger assignment
     */
    @Transactional
    public Notification notifyAdminRangerAssigned(User admin, User ranger, EmergencyAlert alert) {
        String title = "🔔 RANGER ASSIGNED TO ALERT";
        String message = String.format(
                "Ranger %s has been assigned to alert #%d\nType: %s | Priority: %s\nLocation: %.6f, %.6f",
                ranger.getFullName(),
                alert.getId(),
                alert.getAlertType().name(),
                alert.getPriority().name(),
                alert.getLatitude(),
                alert.getLongitude()
        );

        Notification notification = createNotification(
                admin,
                title,
                message,
                "ALERT_ASSIGNED",
                alert.getId(),
                true
        );

        log.info("✅ Admin {} notified about ranger assignment", admin.getEmail());
        return notification;
    }

    /**
     * ✅ Notify admin about ranger responding
     */
    @Transactional
    public Notification notifyAdminRangerResponding(User admin, User ranger, EmergencyAlert alert) {
        String title = "🔔 RANGER RESPONDING";
        String message = String.format(
                "Ranger %s is responding to alert #%d\nType: %s | Priority: %s",
                ranger.getFullName(),
                alert.getId(),
                alert.getAlertType().name(),
                alert.getPriority().name()
        );

        Notification notification = createNotification(
                admin,
                title,
                message,
                "ALERT_RESPONDING",
                alert.getId(),
                true
        );

        log.info("✅ Admin {} notified about ranger responding", admin.getEmail());
        return notification;
    }

    /**
     * ✅ Notify admin about alert resolution
     */
    @Transactional
    public Notification notifyAdminAlertResolved(User admin, EmergencyAlert alert, String notes) {
        String title = "✅ ALERT RESOLVED";
        String message = String.format(
                "Alert #%d has been resolved.\nType: %s\nResolution Notes: %s",
                alert.getId(),
                alert.getAlertType().name(),
                notes != null ? notes : "No additional notes"
        );

        Notification notification = createNotification(
                admin,
                title,
                message,
                "ALERT_RESOLVED",
                alert.getId(),
                true
        );

        log.info("✅ Admin {} notified about alert resolution", admin.getEmail());
        return notification;
    }

    // ===== VISITOR NOTIFICATIONS =====

    /**
     * ✅ Notify visitor about alert creation
     */
    @Transactional
    public Notification notifyVisitorAlertCreated(User visitor, EmergencyAlert alert) {
        String title = "🚨 SOS ALERT SENT";
        String message = String.format(
                "Your emergency alert has been sent.\nType: %s\nWe have dispatched help to your location.\nPlease stay where you are.",
                alert.getAlertType().name()
        );

        Notification notification = createNotification(
                visitor,
                title,
                message,
                "ALERT_CREATED",
                alert.getId(),
                false
        );

        log.info("✅ Visitor {} notified about alert creation", visitor.getEmail());
        return notification;
    }

    /**
     * ✅ Notify visitor about ranger assigned
     */
    @Transactional
    public Notification notifyVisitorRangerAssigned(User visitor, EmergencyAlert alert, User ranger) {
        String title = "🚨 RANGER EN ROUTE";
        String message = String.format(
                "Ranger %s has been dispatched to your location.\nPlease stay where you are and wait for assistance.",
                ranger.getFullName()
        );

        Notification notification = createNotification(
                visitor,
                title,
                message,
                "ALERT_RESPONDING",
                alert.getId(),
                false
        );

        log.info("✅ Visitor {} notified about ranger assignment", visitor.getEmail());
        return notification;
    }

    /**
     * ✅ Notify visitor about alert resolved
     */
    @Transactional
    public Notification notifyVisitorAlertResolved(User visitor, EmergencyAlert alert, String notes) {
        String title = "✅ EMERGENCY RESOLVED";
        String message = String.format(
                "Your emergency alert #%d has been resolved.\n%s",
                alert.getId(),
                notes != null ? "Notes: " + notes : "Thank you for your patience."
        );

        Notification notification = createNotification(
                visitor,
                title,
                message,
                "ALERT_RESOLVED",
                alert.getId(),
                false
        );

        log.info("✅ Visitor {} notified about alert resolution", visitor.getEmail());
        return notification;
    }

    /**
     * ✅ Notify visitor about false alarm
     */
    @Transactional
    public Notification notifyVisitorFalseAlarm(User visitor, EmergencyAlert alert, String notes) {
        String title = "ℹ️ FALSE ALARM";
        String message = String.format(
                "Your emergency alert #%d has been marked as a false alarm.\n%s",
                alert.getId(),
                notes != null ? "Notes: " + notes : "Please only use SOS for actual emergencies."
        );

        Notification notification = createNotification(
                visitor,
                title,
                message,
                "ALERT_FALSE_ALARM",
                alert.getId(),
                false
        );

        log.info("✅ Visitor {} notified about false alarm", visitor.getEmail());
        return notification;
    }

    // ===== EXISTING BROADCAST METHODS =====

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

    @Transactional
    public Notification broadcastToUser(Long userId, String title, String message) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return createNotification(user, title, message, "BROADCAST", null, true);
    }

    @Transactional
    public List<Notification> broadcastToAllUsers(String title, String message) {
        List<User> allUsers = userRepository.findAll();
        return allUsers.stream()
                .map(user -> createNotification(user, title, message, "BROADCAST", null, true))
                .collect(Collectors.toList());
    }

    // ===== NOTIFICATION RETRIEVAL METHODS =====

    public List<NotificationResponse> getUserNotifications(User user) {
        return notificationRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(NotificationResponse::forUser)
                .collect(Collectors.toList());
    }

    public List<NotificationResponse> getUnreadNotifications(User user) {
        return notificationRepository.findByUserAndReadFalseOrderByCreatedAtDesc(user)
                .stream()
                .map(NotificationResponse::forUser)
                .collect(Collectors.toList());
    }

    public long getUnreadCount(User user) {
        return notificationRepository.countByUserAndReadFalse(user);
    }

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

    @Transactional
    public void markAllAsRead(User user) {
        notificationRepository.markAllAsRead(user);
    }

    public List<NotificationResponse> getAllBroadcasts() {
        return notificationRepository.findBroadcastNotifications()
                .stream()
                .map(NotificationResponse::forAdmin)
                .collect(Collectors.toList());
    }

    public long getActiveVisitorsCount() {
        return sessionRepository.countByActiveTrue();
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