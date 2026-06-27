package com.vistara.tourist_tracking_system.service;

import com.vistara.tourist_tracking_system.dto.EmergencyAlertResponse;
import com.vistara.tourist_tracking_system.dto.SOSAlertDTO;
import com.vistara.tourist_tracking_system.exception.DuplicateResourceException;
import com.vistara.tourist_tracking_system.model.EmergencyAlert;
import com.vistara.tourist_tracking_system.model.VisitorSession;
import com.vistara.tourist_tracking_system.model.User;
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

    @Transactional
    public EmergencyAlertResponse triggerSOS(SOSAlertDTO dto) {
        VisitorSession session = sessionRepository.findById(dto.getSessionId())
                .orElseThrow(() -> new DuplicateResourceException("Session not found"));

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

        return convertToResponse(saved);
    }

    public List<EmergencyAlertResponse> getPendingAlerts() {
        List<EmergencyAlert> alerts = alertRepository.findByAlertStatus(EmergencyAlert.AlertStatus.PENDING);
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

        return convertToResponse(saved);
    }

    /**
     * Convert EmergencyAlert entity to EmergencyAlertResponse DTO
     * This excludes the location_point geometry field to avoid JSON serialization errors
     */
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
        response.setResolutionNotes(alert.getResolutionNotes());  // ← This sets the field

        if (alert.getSession() != null) {
            response.setSessionId(alert.getSession().getId());
        }

        if (alert.getUser() != null) {
            response.setVisitorName(alert.getUser().getFullName());
            response.setVisitorPhone(alert.getUser().getPhoneNumber());
        }

        if (alert.getAssignedRanger() != null) {
            response.setAssignedRangerId(alert.getAssignedRanger().getId());
            response.setAssignedRangerName(alert.getAssignedRanger().getFullName());
        }

        return response;
    }
}