package com.vistara.tourist_tracking_system.service;

import com.vistara.tourist_tracking_system.dto.LocationUpdateDTO;
import com.vistara.tourist_tracking_system.model.LocationTracking;
import com.vistara.tourist_tracking_system.model.VisitorSession;
import com.vistara.tourist_tracking_system.repository.LocationTrackingRepository;
import com.vistara.tourist_tracking_system.repository.VisitorSessionRepository;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TrackingService {

    private final LocationTrackingRepository locationRepository;
    private final VisitorSessionRepository sessionRepository;
    private final GeometryFactory geometryFactory = new GeometryFactory();

    /**
     * Update the visitor's location
     */
    @Transactional
    public LocationTracking updateLocation(LocationUpdateDTO dto) {
        // 1. Find the active session
        VisitorSession session = sessionRepository.findById(dto.getSessionId())
                .orElseThrow(() -> new RuntimeException("Session not found"));

        if (!session.isActive()) {
            throw new RuntimeException("Session is not active");
        }

        // 2. Create location point (PostGIS geometry)
        Point locationPoint = geometryFactory.createPoint(
                new Coordinate(dto.getLongitude(), dto.getLatitude())
        );
        locationPoint.setSRID(4326);  // WGS84 coordinate system

        // 3. Create LocationTracking entity
        LocationTracking location = new LocationTracking();
        location.setSession(session);
        location.setLatitude(dto.getLatitude());
        location.setLongitude(dto.getLongitude());
        location.setLocationPoint(locationPoint);
        location.setAccuracy(dto.getAccuracy());
        location.setBatteryLevel(dto.getBatteryLevel());
        location.setTimestamp(LocalDateTime.now());

        // 4. Check if visitor is within geofence (optional)
        // location.setWithinGeofence(checkGeofence(dto.getLatitude(), dto.getLongitude()));

        // 5. Update session's last known location
        session.setLastKnownLocation(locationPoint);
        session.setLastLocationUpdate(LocalDateTime.now());
        sessionRepository.save(session);

        // 6. Save the location tracking record
        return locationRepository.save(location);
    }

    /**
     * Get the last known location for a session
     */
    public LocationTracking getLastLocation(Long sessionId) {
        VisitorSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        LocationTracking lastLocation = locationRepository.findTopBySessionOrderByTimestampDesc(session);

        if (lastLocation == null) {
            throw new RuntimeException("No location found for this session");
        }

        return lastLocation;
    }

    /**
     * Get location history for a session
     */
    public List<LocationTracking> getLocationHistory(Long sessionId, LocalDateTime from, LocalDateTime to) {
        VisitorSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        return locationRepository.findBySessionAndTimestampBetween(session, from, to);
    }
}