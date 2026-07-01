package com.vistara.tourist_tracking_system.service;

import com.vistara.tourist_tracking_system.dto.EmergencyAlertBreakdown;
import com.vistara.tourist_tracking_system.dto.EmergencyAlertResponse;
import com.vistara.tourist_tracking_system.dto.SOSAlertDTO;
import com.vistara.tourist_tracking_system.model.EmergencyAlert;
import com.vistara.tourist_tracking_system.model.User;
import com.vistara.tourist_tracking_system.model.VisitorSession;
import com.vistara.tourist_tracking_system.repository.EmergencyAlertRepository;
import com.vistara.tourist_tracking_system.repository.UserRepository;
import com.vistara.tourist_tracking_system.repository.VisitorSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmergencyService {

    private final EmergencyAlertRepository alertRepository;
    private final VisitorSessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    /**
     * ✅ Create a new emergency alert with notifications
     */
    @Transactional
    public EmergencyAlertResponse createAlert(Long sessionId, EmergencyAlert.AlertType alertType, String message,
                                              Double latitude, Double longitude) {
        VisitorSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Visitor session not found"));

        EmergencyAlert alert = new EmergencyAlert();
        alert.setSession(session);
        alert.setUser(session.getUser());
        alert.setAlertType(alertType);
        alert.setAlertStatus(EmergencyAlert.AlertStatus.PENDING);
        alert.setPriority(EmergencyAlert.AlertPriority.HIGH);
        alert.setLatitude(latitude);
        alert.setLongitude(longitude);
        alert.setMessage(message);
        alert.setCreatedAt(LocalDateTime.now());
        alert.setUpdatedAt(LocalDateTime.now());

        EmergencyAlert savedAlert = alertRepository.save(alert);

        // 🔔 Send notifications
        try {
            // Notify visitor
            notificationService.notifyVisitorAlertCreated(session.getUser(), savedAlert);

            // Notify all rangers
            notificationService.notifyRangersAboutNewAlert(savedAlert);

            // Notify all admins
            notificationService.notifyAdminsAboutNewAlert(savedAlert);

            log.info("✅ Notifications sent for new alert {}", savedAlert.getId());
        } catch (Exception e) {
            log.error("Failed to send notifications for alert {}: {}", savedAlert.getId(), e.getMessage());
        }

        return convertToResponse(savedAlert);
    }

    /**
     * ✅ Assign ranger to alert with notifications
     */
    @Transactional
    public EmergencyAlertResponse assignRanger(Long alertId, User ranger) {
        EmergencyAlert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new RuntimeException("Alert not found"));

        // Check if ranger already has an active alert
        if (hasActiveAlert(ranger)) {
            throw new RuntimeException("Ranger already has an active alert. Please resolve it first.");
        }

        EmergencyAlert.AlertStatus oldStatus = alert.getAlertStatus();
        alert.setAssignedRanger(ranger);
        alert.setAlertStatus(EmergencyAlert.AlertStatus.RESPONDING);
        alert.setRespondedAt(LocalDateTime.now());
        alert.setUpdatedAt(LocalDateTime.now());

        EmergencyAlert savedAlert = alertRepository.save(alert);

        // 🔔 Send notifications
        try {
            // Notify the assigned ranger
            notificationService.notifyRangerAssigned(ranger, savedAlert);

            // Notify all admins
            List<User> admins = userRepository.findByRole(User.Role.ADMIN);
            for (User admin : admins) {
                notificationService.notifyAdminRangerAssigned(admin, ranger, savedAlert);
            }

            // Notify the visitor
            VisitorSession session = savedAlert.getSession();
            if (session != null && session.getUser() != null) {
                notificationService.notifyVisitorRangerAssigned(session.getUser(), savedAlert, ranger);
            }

            // Notify other rangers about status change
            List<User> otherRangers = userRepository.findByRole(User.Role.RANGER)
                    .stream()
                    .filter(r -> !r.getId().equals(ranger.getId()))
                    .collect(Collectors.toList());
            for (User otherRanger : otherRangers) {
                notificationService.notifyRangerStatusChange(otherRanger, savedAlert, oldStatus.name(), "RESPONDING");
            }

            log.info("✅ Notifications sent for alert {} assignment", alertId);
        } catch (Exception e) {
            log.error("Failed to send notifications for alert {}: {}", alertId, e.getMessage());
        }

        return convertToResponse(savedAlert);
    }

    /**
     * ✅ Ranger claims alert with notifications
     */
    @Transactional
    public EmergencyAlertResponse claimAlert(Long alertId, User ranger) {
        EmergencyAlert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new RuntimeException("Alert not found"));

        if (alert.getAlertStatus() != EmergencyAlert.AlertStatus.PENDING) {
            throw new RuntimeException("Alert is not in PENDING status. Current status: " + alert.getAlertStatus());
        }

        if (hasActiveAlert(ranger)) {
            throw new RuntimeException("You already have an active alert. Please resolve it first.");
        }

        EmergencyAlert.AlertStatus oldStatus = alert.getAlertStatus();
        alert.setAssignedRanger(ranger);
        alert.setAlertStatus(EmergencyAlert.AlertStatus.RESPONDING);
        alert.setRespondedAt(LocalDateTime.now());
        alert.setUpdatedAt(LocalDateTime.now());

        EmergencyAlert savedAlert = alertRepository.save(alert);

        // 🔔 Send notifications
        try {
            // Notify the ranger
            notificationService.notifyRangerResponding(ranger, savedAlert);

            // Notify all admins
            List<User> admins = userRepository.findByRole(User.Role.ADMIN);
            for (User admin : admins) {
                notificationService.notifyAdminRangerResponding(admin, ranger, savedAlert);
            }

            // Notify the visitor
            VisitorSession session = savedAlert.getSession();
            if (session != null && session.getUser() != null) {
                notificationService.notifyVisitorRangerAssigned(session.getUser(), savedAlert, ranger);
            }

            // Notify other rangers about status change
            List<User> otherRangers = userRepository.findByRole(User.Role.RANGER)
                    .stream()
                    .filter(r -> !r.getId().equals(ranger.getId()))
                    .collect(Collectors.toList());
            for (User otherRanger : otherRangers) {
                notificationService.notifyRangerStatusChange(otherRanger, savedAlert, oldStatus.name(), "RESPONDING");
            }

            log.info("✅ Notifications sent for alert {} claimed by ranger {}", alertId, ranger.getEmail());
        } catch (Exception e) {
            log.error("Failed to send notifications for alert {}: {}", alertId, e.getMessage());
        }

        return convertToResponse(savedAlert);
    }

    /**
     * ✅ Resolve alert by ranger with notifications
     */
    @Transactional
    public EmergencyAlertResponse resolveAlertByRanger(Long alertId, String notes, User ranger) {
        EmergencyAlert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new RuntimeException("Alert not found"));

        if (alert.getAssignedRanger() == null ||
                !alert.getAssignedRanger().getId().equals(ranger.getId())) {
            throw new RuntimeException("You are not assigned to this alert");
        }

        if (alert.getAlertStatus() == EmergencyAlert.AlertStatus.RESOLVED) {
            throw new RuntimeException("Alert is already resolved");
        }

        // Check for false alarm
        boolean isFalseAlarm = notes != null &&
                (notes.toLowerCase().contains("false alarm") ||
                        notes.toLowerCase().contains("f alse") ||
                        notes.toLowerCase().contains("falsealarm") ||
                        notes.toLowerCase().contains("mistake"));

        EmergencyAlert.AlertStatus oldStatus = alert.getAlertStatus();
        EmergencyAlert.AlertStatus newStatus = isFalseAlarm ?
                EmergencyAlert.AlertStatus.FALSE_ALARM :
                EmergencyAlert.AlertStatus.RESOLVED;

        alert.setAlertStatus(newStatus);
        alert.setResolutionNotes(notes);
        alert.setResolvedAt(LocalDateTime.now());
        alert.setUpdatedAt(LocalDateTime.now());

        EmergencyAlert savedAlert = alertRepository.save(alert);

        // 🔔 Send notifications based on resolution type
        try {
            if (isFalseAlarm) {
                // Notify ranger about false alarm
                notificationService.notifyRangerFalseAlarm(ranger, savedAlert, notes);

                // Notify visitor about false alarm
                VisitorSession session = savedAlert.getSession();
                if (session != null && session.getUser() != null) {
                    notificationService.notifyVisitorFalseAlarm(session.getUser(), savedAlert, notes);
                }

                // Notify admins about false alarm
                List<User> admins = userRepository.findByRole(User.Role.ADMIN);
                for (User admin : admins) {
                    notificationService.notifyAdminAlertResolved(admin, savedAlert, notes);
                }

                // Notify other rangers about false alarm
                List<User> otherRangers = userRepository.findByRole(User.Role.RANGER)
                        .stream()
                        .filter(r -> !r.getId().equals(ranger.getId()))
                        .collect(Collectors.toList());
                for (User otherRanger : otherRangers) {
                    notificationService.notifyRangerStatusChange(otherRanger, savedAlert, oldStatus.name(), "FALSE_ALARM");
                }

                log.info("✅ Alert {} marked as FALSE ALARM by ranger {}", alertId, ranger.getEmail());
            } else {
                // Notify ranger about resolution
                notificationService.notifyRangerResolved(ranger, savedAlert, notes);

                // Notify visitor about resolution
                VisitorSession session = savedAlert.getSession();
                if (session != null && session.getUser() != null) {
                    notificationService.notifyVisitorAlertResolved(session.getUser(), savedAlert, notes);
                }

                // Notify admins about resolution
                List<User> admins = userRepository.findByRole(User.Role.ADMIN);
                for (User admin : admins) {
                    notificationService.notifyAdminAlertResolved(admin, savedAlert, notes);
                }

                // Notify other rangers about resolution
                List<User> otherRangers = userRepository.findByRole(User.Role.RANGER)
                        .stream()
                        .filter(r -> !r.getId().equals(ranger.getId()))
                        .collect(Collectors.toList());
                for (User otherRanger : otherRangers) {
                    notificationService.notifyRangerStatusChange(otherRanger, savedAlert, oldStatus.name(), "RESOLVED");
                }

                log.info("✅ Alert {} resolved by ranger {}", alertId, ranger.getEmail());
            }
        } catch (Exception e) {
            log.error("Failed to send notifications for alert {}: {}", alertId, e.getMessage());
        }

        return convertToResponse(savedAlert);
    }

    /**
     * ✅ Resolve alert (admin) with notifications
     */
    @Transactional
    public EmergencyAlertResponse resolveAlert(Long alertId, String notes) {
        EmergencyAlert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new RuntimeException("Alert not found"));

        if (alert.getAlertStatus() == EmergencyAlert.AlertStatus.RESOLVED) {
            throw new RuntimeException("Alert is already resolved");
        }

        boolean isFalseAlarm = notes != null &&
                (notes.toLowerCase().contains("false alarm") ||
                        notes.toLowerCase().contains("f alse") ||
                        notes.toLowerCase().contains("falsealarm") ||
                        notes.toLowerCase().contains("mistake"));

        EmergencyAlert.AlertStatus oldStatus = alert.getAlertStatus();
        EmergencyAlert.AlertStatus newStatus = isFalseAlarm ?
                EmergencyAlert.AlertStatus.FALSE_ALARM :
                EmergencyAlert.AlertStatus.RESOLVED;

        alert.setAlertStatus(newStatus);
        alert.setResolutionNotes(notes);
        alert.setResolvedAt(LocalDateTime.now());
        alert.setUpdatedAt(LocalDateTime.now());

        EmergencyAlert savedAlert = alertRepository.save(alert);

        // 🔔 Send notifications
        try {
            // Notify visitor
            VisitorSession session = savedAlert.getSession();
            if (session != null && session.getUser() != null) {
                if (isFalseAlarm) {
                    notificationService.notifyVisitorFalseAlarm(session.getUser(), savedAlert, notes);
                } else {
                    notificationService.notifyVisitorAlertResolved(session.getUser(), savedAlert, notes);
                }
            }

            // Notify assigned ranger if any
            if (savedAlert.getAssignedRanger() != null) {
                if (isFalseAlarm) {
                    notificationService.notifyRangerFalseAlarm(savedAlert.getAssignedRanger(), savedAlert, notes);
                } else {
                    notificationService.notifyRangerResolved(savedAlert.getAssignedRanger(), savedAlert, notes);
                }
            }

            // Notify other rangers about status change
            List<User> otherRangers = userRepository.findByRole(User.Role.RANGER)
                    .stream()
                    .filter(r -> savedAlert.getAssignedRanger() == null ||
                            !r.getId().equals(savedAlert.getAssignedRanger().getId()))
                    .collect(Collectors.toList());
            for (User otherRanger : otherRangers) {
                notificationService.notifyRangerStatusChange(otherRanger, savedAlert, oldStatus.name(), newStatus.name());
            }

            log.info("✅ Notifications sent for alert {} resolution", alertId);
        } catch (Exception e) {
            log.error("Failed to send notifications for alert {}: {}", alertId, e.getMessage());
        }

        return convertToResponse(savedAlert);
    }

    // ===== EXISTING METHODS =====

    public boolean hasActiveAlert(User ranger) {
        List<EmergencyAlert> activeAlerts = alertRepository.findByAssignedRangerAndAlertStatus(
                ranger,
                EmergencyAlert.AlertStatus.RESPONDING
        );
        return !activeAlerts.isEmpty();
    }

    public EmergencyAlertResponse getCurrentActiveAlert(User ranger) {
        List<EmergencyAlert> activeAlerts = alertRepository.findByAssignedRangerAndAlertStatus(
                ranger,
                EmergencyAlert.AlertStatus.RESPONDING
        );
        if (activeAlerts.isEmpty()) {
            return null;
        }
        return convertToResponse(activeAlerts.get(0));
    }

    public List<EmergencyAlertResponse> getAllAlerts() {
        return alertRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<EmergencyAlertResponse> getAlertsByRanger(User ranger) {
        return alertRepository.findByAssignedRangerOrderByCreatedAtDesc(ranger)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<EmergencyAlertResponse> getAlertsByRangerAndStatus(User ranger, EmergencyAlert.AlertStatus status) {
        return alertRepository.findByAssignedRangerAndAlertStatus(ranger, status)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<EmergencyAlertResponse> getPendingAlerts() {
        return alertRepository.findByAlertStatus(EmergencyAlert.AlertStatus.PENDING)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<EmergencyAlertResponse> getAlertsByStatus(EmergencyAlert.AlertStatus status) {
        return alertRepository.findByAlertStatusOrderByCreatedAtDesc(status)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public EmergencyAlertResponse getAlertById(Long alertId) {
        EmergencyAlert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new RuntimeException("Alert not found"));
        return convertToResponse(alert);
    }

    public List<EmergencyAlertBreakdown> getAlertBreakdown() {
        List<Object[]> results = alertRepository.countByStatusAndPriority();
        return results.stream()
                .map(row -> {
                    EmergencyAlertBreakdown breakdown = new EmergencyAlertBreakdown();
                    breakdown.setStatus(((EmergencyAlert.AlertStatus) row[0]).name());
                    breakdown.setPriority(((EmergencyAlert.AlertPriority) row[1]).name());
                    breakdown.setCount((Long) row[2]);
                    return breakdown;
                })
                .collect(Collectors.toList());
    }

    /**
     * ✅ Trigger SOS alert from visitor
     */
    @Transactional
    public EmergencyAlertResponse triggerSOS(SOSAlertDTO dto) {
        VisitorSession session = sessionRepository.findById(dto.getSessionId())
                .orElseThrow(() -> new RuntimeException("Visitor session not found"));

        EmergencyAlert.AlertType alertType;
        try {
            alertType = EmergencyAlert.AlertType.valueOf(dto.getAlertType().toUpperCase());
        } catch (IllegalArgumentException e) {
            alertType = EmergencyAlert.AlertType.GENERAL_DISTRESS;
        }

        EmergencyAlert alert = new EmergencyAlert();
        alert.setSession(session);
        alert.setUser(session.getUser());
        alert.setAlertType(alertType);
        alert.setAlertStatus(EmergencyAlert.AlertStatus.PENDING);
        alert.setPriority(EmergencyAlert.AlertPriority.HIGH);
        alert.setLatitude(dto.getLatitude());
        alert.setLongitude(dto.getLongitude());
        alert.setMessage(dto.getMessage());
        alert.setCreatedAt(LocalDateTime.now());
        alert.setUpdatedAt(LocalDateTime.now());

        EmergencyAlert savedAlert = alertRepository.save(alert);

        // 🔔 Send notifications
        try {
            // Notify visitor
            notificationService.notifyVisitorAlertCreated(session.getUser(), savedAlert);

            // Notify all rangers
            notificationService.notifyRangersAboutNewAlert(savedAlert);

            // Notify all admins
            notificationService.notifyAdminsAboutNewAlert(savedAlert);

            log.info("✅ SOS alert {} triggered by visitor {}", savedAlert.getId(), session.getUser().getEmail());
        } catch (Exception e) {
            log.error("Failed to send notifications for SOS alert {}: {}", savedAlert.getId(), e.getMessage());
        }

        return convertToResponse(savedAlert);
    }

    private EmergencyAlertResponse convertToResponse(EmergencyAlert alert) {
        EmergencyAlertResponse response = new EmergencyAlertResponse();
        response.setId(alert.getId());
        response.setAlertType(alert.getAlertType().name());
        response.setAlertStatus(alert.getAlertStatus().name());
        response.setPriority(alert.getPriority().name());
        response.setLatitude(alert.getLatitude());
        response.setLongitude(alert.getLongitude());
        response.setMessage(alert.getMessage());
        response.setSessionId(alert.getSession().getId());
        response.setResolutionNotes(alert.getResolutionNotes());
        response.setCreatedAt(alert.getCreatedAt());
        response.setRespondedAt(alert.getRespondedAt());
        response.setResolvedAt(alert.getResolvedAt());

        if (alert.getAssignedRanger() != null) {
            response.setAssignedRangerId(alert.getAssignedRanger().getId());
            response.setAssignedRangerName(alert.getAssignedRanger().getFullName());
        }

        if (alert.getSession() != null && alert.getSession().getUser() != null) {
            User visitor = alert.getSession().getUser();
            response.setVisitorName(visitor.getFullName());
            response.setVisitorPhone(visitor.getPhoneNumber());

            // Emergency contact details
            response.setEmergencyContactName(visitor.getEmergencyContactName());
            response.setEmergencyContactPhone(visitor.getEmergencyContactPhone());
        }

        // Calculate response time
        if (alert.getRespondedAt() != null && alert.getCreatedAt() != null) {
            long seconds = java.time.Duration.between(alert.getCreatedAt(), alert.getRespondedAt()).getSeconds();
            response.setResponseTimeSeconds((int) seconds);
        }

        return response;
    }
}