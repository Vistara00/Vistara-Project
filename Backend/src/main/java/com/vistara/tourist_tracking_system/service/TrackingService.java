package com.vistara.tourist_tracking_system.service;

import com.vistara.tourist_tracking_system.dto.LocationUpdateDTO;
import com.vistara.tourist_tracking_system.model.LocationTracking;
import com.vistara.tourist_tracking_system.model.VisitorSession;
import com.vistara.tourist_tracking_system.repository.LocationTrackingRepository;
import com.vistara.tourist_tracking_system.repository.VisitorSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TrackingService {

    private final LocationTrackingRepository locationRepository;
    private final VisitorSessionRepository sessionRepository;

    @Transactional
    public LocationTracking updateLocation(LocationUpdateDTO dto) {
        VisitorSession session = sessionRepository.findById(dto.getSessionId())
                .orElseThrow(() -> new RuntimeException("Session not found"));

        LocationTracking location = new LocationTracking();
        location.setSession(session);
        location.setLatitude(dto.getLatitude());
        location.setLongitude(dto.getLongitude());
        location.setAccuracy(dto.getAccuracy());
        location.setTimestamp(LocalDateTime.now());
        location.setBatteryLevel(dto.getBatteryLevel());

        // Update session's last known location
        session.setLastKnownLocation(String.format("POINT(%f %f)", dto.getLongitude(), dto.getLatitude()));
        sessionRepository.save(session);

        return locationRepository.save(location);
    }

    public LocationTracking getLastLocation(Long sessionId) {
        VisitorSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));
        return locationRepository.findTopBySessionOrderByTimestampDesc(session);
    }

    public List<LocationTracking> getLocationHistory(Long sessionId, LocalDateTime from, LocalDateTime to) {
        VisitorSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));
        return locationRepository.findBySessionOrderByTimestampDesc(session)
                .stream()
                .filter(loc -> loc.getTimestamp().isAfter(from) && loc.getTimestamp().isBefore(to))
                .toList();
    }
}