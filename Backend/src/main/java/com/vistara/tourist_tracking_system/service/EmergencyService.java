package com.vistara.tourist_tracking_system.service;

import com.vistara.tourist_tracking_system.dto.SOSAlertDTO;
import com.vistara.tourist_tracking_system.exception.DuplicateResourceException;
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
                .orElseThrow(() -> new DuplicateResourceException("Session not found"));

        EmergencyAlert alert = new EmergencyAlert();
        alert.setSession(session);
        alert.setUser(session.getUser());           // user_id is required (NOT NULL in DB)
        alert.setAlertType(dto.getAlertType());
        alert.setLatitude(dto.getLatitude());
        alert.setLongitude(dto.getLongitude());
        alert.setMessage(dto.getMessage());
        alert.setAlertStatus(EmergencyAlert.AlertStatus.PENDING);   // was setStatus()
        alert.setPriority(EmergencyAlert.AlertPriority.HIGH);       // priority NOT NULL default

        // Mark session as SOS triggered
        session.setSosTriggered(true);
        session.setHasEmergency(true);
        sessionRepository.save(session);

        return alertRepository.save(alert);
    }

    public List<EmergencyAlert> getPendingAlerts() {
        return alertRepository.findByAlertStatus(EmergencyAlert.AlertStatus.PENDING); // was findByStatus()
    }

    @Transactional
    public EmergencyAlert assignRanger(Long alertId, User ranger) {
        EmergencyAlert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new DuplicateResourceException("Alert not found"));

        alert.setAssignedRanger(ranger);
        alert.setAlertStatus(EmergencyAlert.AlertStatus.RESPONDING);    // was setStatus()
        alert.setRespondedAt(LocalDateTime.now());

        return alertRepository.save(alert);
    }

    @Transactional
    public EmergencyAlert resolveAlert(Long alertId, String notes) {
        EmergencyAlert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new DuplicateResourceException("Alert not found"));

        alert.setAlertStatus(EmergencyAlert.AlertStatus.RESOLVED);      // was setStatus()
        alert.setResolvedAt(LocalDateTime.now());
        alert.setResolutionNotes(notes);

        return alertRepository.save(alert);
    }
}