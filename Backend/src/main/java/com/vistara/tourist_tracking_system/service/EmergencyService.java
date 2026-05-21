package com.vistara.tourist_tracking_system.service;

import com.vistara.tourist_tracking_system.dto.SOSAlertDTO;
import com.vistara.tourist_tracking_system.model.EmergencyAlert;
import com.vistara.tourist_tracking_system.model.VisitorSession;
import com.vistara.tourist_tracking_system.model.User;
import com.vistara.tourist_tracking_system.repository.EmergencyAlertRepository;
import com.vistara.tourist_tracking_system.repository.VisitorSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmergencyService {

    private final EmergencyAlertRepository alertRepository;
    private final VisitorSessionRepository sessionRepository;

    @Transactional
    public EmergencyAlert triggerSOS(SOSAlertDTO dto) {
        VisitorSession session = sessionRepository.findById(dto.getSessionId())
                .orElseThrow(() -> new RuntimeException("Session not found"));

        EmergencyAlert alert = new EmergencyAlert();
        alert.setSession(session);
        alert.setAlertType(dto.getAlertType());
        alert.setLatitude(dto.getLatitude());
        alert.setLongitude(dto.getLongitude());
        alert.setMessage(dto.getMessage());
        alert.setTimestamp(LocalDateTime.now());
        alert.setStatus(EmergencyAlert.AlertStatus.PENDING);

        // Mark session has SOS triggered
        session.setSosTriggered(true);
        sessionRepository.save(session);

        return alertRepository.save(alert);
    }

    public List<EmergencyAlert> getPendingAlerts() {
        return alertRepository.findByStatus(EmergencyAlert.AlertStatus.PENDING);
    }

    @Transactional
    public EmergencyAlert assignRanger(Long alertId, User ranger) {
        EmergencyAlert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new RuntimeException("Alert not found"));
        alert.setAssignedRanger(ranger);
        alert.setStatus(EmergencyAlert.AlertStatus.RESPONDING);
        return alertRepository.save(alert);
    }

    @Transactional
    public EmergencyAlert resolveAlert(Long alertId, String notes) {
        EmergencyAlert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new RuntimeException("Alert not found"));
        alert.setStatus(EmergencyAlert.AlertStatus.RESOLVED);
        alert.setResolvedAt(LocalDateTime.now());
        alert.setResolutionNotes(notes);
        return alertRepository.save(alert);
    }
}