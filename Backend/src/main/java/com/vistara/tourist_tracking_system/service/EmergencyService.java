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

        EmergencyAlert savedAlert = alertRepository.save(alert);

        // 🔔 Send notifications
        try {
            notificationService.notifyVisitorAlertCreated(session.getUser(), savedAlert);
            notificationService.notifyRangersAboutNewAlert(savedAlert);
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

        if (hasActiveAlert(ranger)) {
            throw new RuntimeException("Ranger already has an active alert. Please resolve it first.");
        }

        alert.setAssignedRanger(ranger);
        alert.setAlertStatus(EmergencyAlert.AlertStatus.RESPONDING);
        alert.setRespondedAt(LocalDateTime.now());

        EmergencyAlert savedAlert = alertRepository.save(alert);

        // 🔔 Send notifications
        try {
            notificationService.notifyRangerAssigned(ranger, savedAlert);
            List<User> admins = userRepository.findByRole(User.Role.ADMIN);
            for (User admin : admins) {
                notificationService.notifyAdminRangerAssigned(admin, ranger, savedAlert);
            }
            VisitorSession session = savedAlert.getSession();
            if (session != null && session.getUser() != null) {
                notificationService.notifyVisitorRangerAssigned(session.getUser(), savedAlert, ranger);
            }
            log.info("✅ Notifications sent for alert {} assignment", alertId);
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

        EmergencyAlert.AlertStatus newStatus = isFalseAlarm ?
                EmergencyAlert.AlertStatus.FALSE_ALARM :
                EmergencyAlert.AlertStatus.RESOLVED;

        alert.setAlertStatus(newStatus);
        alert.setResolutionNotes(notes);
        alert.setResolvedAt(LocalDateTime.now());

        EmergencyAlert savedAlert = alertRepository.save(alert);

        // 🔔 Send notifications
        try {
            VisitorSession session = savedAlert.getSession();
            if (session != null && session.getUser() != null) {
                if (isFalseAlarm) {
                    notificationService.notifyVisitorFalseAlarm(session.getUser(), savedAlert, notes);
                } else {
                    notificationService.notifyVisitorAlertResolved(session.getUser(), savedAlert, notes);
                }
            }
            if (savedAlert.getAssignedRanger() != null) {
                if (isFalseAlarm) {
                    notificationService.notifyRangerFalseAlarm(savedAlert.getAssignedRanger(), savedAlert, notes);
                } else {
                    notificationService.notifyRangerResolved(savedAlert.getAssignedRanger(), savedAlert, notes);
                }
            }
            log.info("✅ Notifications sent for alert {} resolution", alertId);
        } catch (Exception e) {
            log.error("Failed to send notifications for alert {}: {}", alertId, e.getMessage());
        }

        return convertToResponse(savedAlert);
    }

    /**
     * ✅ Get active alerts (PENDING or RESPONDING)
     */
    public List<EmergencyAlertResponse> getActiveAlerts() {
        List<EmergencyAlert> alerts = alertRepository.findByAlertStatusIn(
                List.of(EmergencyAlert.AlertStatus.PENDING, EmergencyAlert.AlertStatus.RESPONDING)
        );
        return alerts.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * ✅ Get alerts with filters
     */
    public List<EmergencyAlertResponse> getAlerts(String status, LocalDateTime from, LocalDateTime to) {
        List<EmergencyAlert> alerts;
        if (status != null && !status.isEmpty()) {
            try {
                EmergencyAlert.AlertStatus alertStatus = EmergencyAlert.AlertStatus.valueOf(status.toUpperCase());
                alerts = alertRepository.findByAlertStatusOrderByCreatedAtDesc(alertStatus);
            } catch (IllegalArgumentException e) {
                alerts = alertRepository.findAllByOrderByCreatedAtDesc();
            }
        } else {
            alerts = alertRepository.findAllByOrderByCreatedAtDesc();
        }

        return alerts.stream()
                .filter(alert -> {
                    boolean matches = true;
                    if (from != null && alert.getCreatedAt().isBefore(from)) {
                        matches = false;
                    }
                    if (to != null && alert.getCreatedAt().isAfter(to)) {
                        matches = false;
                    }
                    return matches;
                })
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * ✅ Get alerts by ranger (using User object)
     */
    public List<EmergencyAlertResponse> getAlertsByRanger(User ranger) {
        List<EmergencyAlert> alerts = alertRepository.findByAssignedRangerOrderByCreatedAtDesc(ranger);
        return alerts.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * ✅ Get alerts by ranger ID
     */
    public List<EmergencyAlertResponse> getAlertsByRanger(Long rangerId) {
        User ranger = userRepository.findById(rangerId)
                .orElseThrow(() -> new RuntimeException("Ranger not found"));
        List<EmergencyAlert> alerts = alertRepository.findByAssignedRangerOrderByCreatedAtDesc(ranger);
        return alerts.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * ✅ Get alerts by ranger and status
     */
    public List<EmergencyAlertResponse> getAlertsByRangerAndStatus(User ranger, EmergencyAlert.AlertStatus status) {
        List<EmergencyAlert> alerts = alertRepository.findByAssignedRangerAndAlertStatus(ranger, status);
        return alerts.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * ✅ Get pending alerts
     */
    public List<EmergencyAlertResponse> getPendingAlerts() {
        List<EmergencyAlert> alerts = alertRepository.findByAlertStatus(EmergencyAlert.AlertStatus.PENDING);
        return alerts.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * ✅ Get alerts by status
     */
    public List<EmergencyAlertResponse> getAlertsByStatus(EmergencyAlert.AlertStatus status) {
        List<EmergencyAlert> alerts = alertRepository.findByAlertStatusOrderByCreatedAtDesc(status);
        return alerts.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * ✅ Check if ranger has active alert
     */
    public boolean hasActiveAlertAssigned(Long rangerId) {
        User ranger = userRepository.findById(rangerId)
                .orElseThrow(() -> new RuntimeException("Ranger not found"));
        List<EmergencyAlert> activeAlerts = alertRepository.findByAssignedRangerAndAlertStatus(
                ranger,
                EmergencyAlert.AlertStatus.RESPONDING
        );
        return !activeAlerts.isEmpty();
    }

    /**
     * ✅ Check if ranger has active alert (using User object)
     */
    public boolean hasActiveAlert(User ranger) {
        List<EmergencyAlert> activeAlerts = alertRepository.findByAssignedRangerAndAlertStatus(
                ranger,
                EmergencyAlert.AlertStatus.RESPONDING
        );
        return !activeAlerts.isEmpty();
    }

    /**
     * ✅ Get current active alert for ranger
     */
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

    /**
     * ✅ Get alert by ID
     */
    public EmergencyAlertResponse getAlertById(Long alertId) {
        EmergencyAlert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new RuntimeException("Alert not found"));
        return convertToResponse(alert);
    }

    /**
     * ✅ Get all alerts
     */
    public List<EmergencyAlertResponse> getAllAlerts() {
        return alertRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * ✅ Get alert breakdown by status and priority
     */
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

        EmergencyAlert savedAlert = alertRepository.save(alert);

        // 🔔 Send notifications
        try {
            notificationService.notifyVisitorAlertCreated(session.getUser(), savedAlert);
            notificationService.notifyRangersAboutNewAlert(savedAlert);
            notificationService.notifyAdminsAboutNewAlert(savedAlert);
            log.info("✅ SOS alert {} triggered by visitor {}", savedAlert.getId(), session.getUser().getEmail());
        } catch (Exception e) {
            log.error("Failed to send notifications for SOS alert {}: {}", savedAlert.getId(), e.getMessage());
        }

        return convertToResponse(savedAlert);
    }

    /**
     * ✅ Resolve alert by ranger
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

        boolean isFalseAlarm = notes != null &&
                (notes.toLowerCase().contains("false alarm") ||
                        notes.toLowerCase().contains("f alse") ||
                        notes.toLowerCase().contains("falsealarm") ||
                        notes.toLowerCase().contains("mistake"));

        EmergencyAlert.AlertStatus newStatus = isFalseAlarm ?
                EmergencyAlert.AlertStatus.FALSE_ALARM :
                EmergencyAlert.AlertStatus.RESOLVED;

        alert.setAlertStatus(newStatus);
        alert.setResolutionNotes(notes);
        alert.setResolvedAt(LocalDateTime.now());

        EmergencyAlert savedAlert = alertRepository.save(alert);

        // Send notifications
        try {
            VisitorSession session = savedAlert.getSession();
            if (session != null && session.getUser() != null) {
                if (isFalseAlarm) {
                    notificationService.notifyVisitorFalseAlarm(session.getUser(), savedAlert, notes);
                } else {
                    notificationService.notifyVisitorAlertResolved(session.getUser(), savedAlert, notes);
                }
            }
            if (isFalseAlarm) {
                notificationService.notifyRangerFalseAlarm(ranger, savedAlert, notes);
            } else {
                notificationService.notifyRangerResolved(ranger, savedAlert, notes);
            }
            log.info("✅ Alert {} resolved by ranger {}", alertId, ranger.getEmail());
        } catch (Exception e) {
            log.error("Failed to send notifications for alert {}: {}", alertId, e.getMessage());
        }

        return convertToResponse(savedAlert);
    }

    /**
     * ✅ Claim alert by ranger
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

        alert.setAssignedRanger(ranger);
        alert.setAlertStatus(EmergencyAlert.AlertStatus.RESPONDING);
        alert.setRespondedAt(LocalDateTime.now());

        EmergencyAlert savedAlert = alertRepository.save(alert);

        // Send notifications
        try {
            notificationService.notifyRangerResponding(ranger, savedAlert);
            VisitorSession session = savedAlert.getSession();
            if (session != null && session.getUser() != null) {
                notificationService.notifyVisitorRangerAssigned(session.getUser(), savedAlert, ranger);
            }
            log.info("✅ Alert {} claimed by ranger {}", alertId, ranger.getEmail());
        } catch (Exception e) {
            log.error("Failed to send notifications for alert {}: {}", alertId, e.getMessage());
        }

        return convertToResponse(savedAlert);
    }

    // ===== Helper Methods =====

    private EmergencyAlertResponse convertToResponse(EmergencyAlert alert) {
        EmergencyAlertResponse response = new EmergencyAlertResponse();
        response.setId(alert.getId());
        response.setAlertType(alert.getAlertType() != null ? alert.getAlertType().name() : null);
        response.setAlertStatus(alert.getAlertStatus() != null ? alert.getAlertStatus().name() : null);
        response.setPriority(alert.getPriority() != null ? alert.getPriority().name() : null);
        response.setLatitude(alert.getLatitude());
        response.setLongitude(alert.getLongitude());
        response.setMessage(alert.getMessage());
        response.setResolutionNotes(alert.getResolutionNotes());
        response.setCreatedAt(alert.getCreatedAt());
        response.setRespondedAt(alert.getRespondedAt());
        response.setResolvedAt(alert.getResolvedAt());
        response.setResponseTimeSeconds(alert.getResponseTimeSeconds());

        if (alert.getSession() != null) {
            response.setSessionId(alert.getSession().getId());
            if (alert.getSession().getUser() != null) {
                User visitor = alert.getSession().getUser();
                response.setVisitorName(visitor.getFullName());
                response.setVisitorEmail(visitor.getEmail());
                response.setVisitorPhone(visitor.getPhoneNumber());
                response.setEmergencyContactName(visitor.getEmergencyContactName());
                response.setEmergencyContactPhone(visitor.getEmergencyContactPhone());
            }
        }

        if (alert.getAssignedRanger() != null) {
            response.setAssignedRangerId(alert.getAssignedRanger().getId());
            response.setAssignedRangerName(alert.getAssignedRanger().getFullName());
        }

        return response;
    }
}