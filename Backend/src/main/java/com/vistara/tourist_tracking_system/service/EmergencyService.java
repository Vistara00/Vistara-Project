package com.vistara.tourist_tracking_system.service;

import com.vistara.tourist_tracking_system.dto.EmergencyAlertResponse;
import com.vistara.tourist_tracking_system.dto.SOSAlertDTO;
import com.vistara.tourist_tracking_system.exception.DuplicateResourceException;
import com.vistara.tourist_tracking_system.model.EmergencyAlert;
import com.vistara.tourist_tracking_system.model.User;
import com.vistara.tourist_tracking_system.model.VisitorSession;
import com.vistara.tourist_tracking_system.repository.EmergencyAlertRepository;
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
    private final NotificationService notificationService;

    @Transactional
    public EmergencyAlertResponse triggerSOS(SOSAlertDTO dto) {
        VisitorSession session = sessionRepository.findById(dto.getSessionId())
                .orElseThrow(() -> new DuplicateResourceException("Session not found"));

        // Create the emergency alert
        EmergencyAlert alert = new EmergencyAlert();
        alert.setSession(session);
        alert.setUser(session.getUser());
        alert.setAlertType(dto.getAlertType());
        alert.setLatitude(dto.getLatitude());
        alert.setLongitude(dto.getLongitude());
        alert.setMessage(dto.getMessage());
        alert.setAlertStatus(EmergencyAlert.AlertStatus.PENDING);
        alert.setPriority(EmergencyAlert.AlertPriority.HIGH);

        // Mark session as SOS triggered
        session.setSosTriggered(true);
        session.setHasEmergency(true);
        sessionRepository.save(session);

        EmergencyAlert saved = alertRepository.save(alert);
        log.info("SOS alert triggered for session {}: {}", dto.getSessionId(), saved.getId());

        // ===== Send Notifications =====

        // 1. Notify the visitor that SOS was sent successfully (CONFIRMATION)
        notificationService.createNotification(
                session.getUser(),
                "🆘 SOS Alert Sent",
                "Your SOS alert has been successfully sent. Rangers have been notified. Stay calm and wait for assistance.\n\n" +
                        "Alert Type: " + dto.getAlertType() + "\n" +
                        "Location: " + dto.getLatitude() + ", " + dto.getLongitude(),
                "ALERT",
                saved.getId(),
                false
        );

        // 2. Notify all admins about the SOS alert (by email)
        notificationService.createNotificationByEmail(
                "admin@vistara.com",
                "🚨 SOS Alert Triggered",
                "Visitor " + session.getUser().getFullName() + " triggered an SOS alert.\n" +
                        "Alert Type: " + dto.getAlertType() + "\n" +
                        "Phone: " + session.getUser().getPhoneNumber() + "\n" +
                        "Location: " + dto.getLatitude() + ", " + dto.getLongitude() + "\n" +
                        "Message: " + (dto.getMessage() != null ? dto.getMessage() : "No additional message"),
                "ALERT",
                saved.getId(),
                false
        );

        // 3. Notify all rangers (by email) about the SOS alert
        // This would require a list of rangers - for now, send to admin
        notificationService.createNotificationByEmail(
                "ranger@vistara.com",
                "🚨 SOS Alert - Ranger Notification",
                "An SOS alert has been triggered by " + session.getUser().getFullName() + ".\n" +
                        "Alert Type: " + dto.getAlertType() + "\n" +
                        "Phone: " + session.getUser().getPhoneNumber() + "\n" +
                        "Location: " + dto.getLatitude() + ", " + dto.getLongitude() + "\n\n" +
                        "Please respond immediately.",
                "ALERT",
                saved.getId(),
                false
        );

        log.info("Notifications sent for SOS alert {}", saved.getId());
        return convertToResponse(saved);
    }

    public List<EmergencyAlertResponse> getPendingAlerts() {
        List<EmergencyAlert> alerts = alertRepository.findByAlertStatus(EmergencyAlert.AlertStatus.PENDING);
        return alerts.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<EmergencyAlertResponse> getAllAlerts() {
        List<EmergencyAlert> alerts = alertRepository.findAllByOrderByCreatedAtDesc();
        return alerts.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public EmergencyAlertResponse assignRanger(Long alertId, User ranger) {
        EmergencyAlert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new DuplicateResourceException("Alert not found"));

        alert.setAssignedRanger(ranger);
        alert.setAlertStatus(EmergencyAlert.AlertStatus.RESPONDING);
        alert.setRespondedAt(LocalDateTime.now());

        EmergencyAlert saved = alertRepository.save(alert);
        log.info("Ranger {} assigned to alert {}", ranger.getEmail(), alertId);

        // Notify the visitor that a ranger is on the way
        notificationService.createNotification(
                alert.getUser(),
                "🚑 Ranger Responding",
                "A ranger has been dispatched to your location. Help is on the way.\n\n" +
                        "Assigned Ranger: " + ranger.getFullName() + "\n" +
                        "Ranger Phone: " + ranger.getPhoneNumber(),
                "ALERT",
                alertId,
                false
        );

        return convertToResponse(saved);
    }

    @Transactional
    public EmergencyAlertResponse resolveAlert(Long alertId, String notes) {
        EmergencyAlert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new DuplicateResourceException("Alert not found"));

        alert.setAlertStatus(EmergencyAlert.AlertStatus.RESOLVED);
        alert.setResolvedAt(LocalDateTime.now());
        alert.setResolutionNotes(notes);

        EmergencyAlert saved = alertRepository.save(alert);
        log.info("Alert {} resolved", alertId);

        // Notify the visitor that the emergency is resolved
        notificationService.createNotification(
                alert.getUser(),
                "✅ Emergency Resolved",
                "The emergency has been resolved. You are safe now.\n\n" +
                        "Resolution Notes: " + (notes != null ? notes : "No additional notes"),
                "ALERT",
                alertId,
                false
        );

        return convertToResponse(saved);
    }

    private EmergencyAlertResponse convertToResponse(EmergencyAlert alert) {
        EmergencyAlertResponse response = new EmergencyAlertResponse();
        response.setId(alert.getId());
        response.setAlertType(alert.getAlertType() != null ? alert.getAlertType().name() : null);
        response.setAlertStatus(alert.getAlertStatus() != null ? alert.getAlertStatus().name() : null);
        response.setPriority(alert.getPriority() != null ? alert.getPriority().name() : null);
        response.setLatitude(alert.getLatitude());
        response.setLongitude(alert.getLongitude());
        response.setMessage(alert.getMessage());
        response.setCreatedAt(alert.getCreatedAt());
        response.setRespondedAt(alert.getRespondedAt());
        response.setResolvedAt(alert.getResolvedAt());
        response.setResponseTimeSeconds(alert.getResponseTimeSeconds());
        response.setResolutionNotes(alert.getResolutionNotes());

        if (alert.getSession() != null) {
            response.setSessionId(alert.getSession().getId());
        }

        if (alert.getUser() != null) {
            User user = alert.getUser();
            response.setVisitorName(user.getFullName());
            response.setVisitorPhone(user.getPhoneNumber());
            response.setEmergencyContactName(user.getEmergencyContactName());
            response.setEmergencyContactPhone(user.getEmergencyContactPhone());
        }

        if (alert.getAssignedRanger() != null) {
            response.setAssignedRangerId(alert.getAssignedRanger().getId());
            response.setAssignedRangerName(alert.getAssignedRanger().getFullName());
        }

        return response;
    }
}