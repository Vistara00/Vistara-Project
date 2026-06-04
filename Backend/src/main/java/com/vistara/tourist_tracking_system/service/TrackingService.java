package com.vistara.tourist_tracking_system.service;

import com.vistara.tourist_tracking_system.dto.LocationUpdateDTO;
import com.vistara.tourist_tracking_system.exception.DuplicateResourceException;
import com.vistara.tourist_tracking_system.model.LocationTracking;
import com.vistara.tourist_tracking_system.model.VisitorSession;
import com.vistara.tourist_tracking_system.repository.LocationTrackingRepository;
import com.vistara.tourist_tracking_system.repository.VisitorSessionRepository;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TrackingService {

    private final LocationTrackingRepository locationRepository;
    private final VisitorSessionRepository sessionRepository;

    // FIX: GeometryFactory with SRID 4326 (WGS84) to match the DB column
    // geometry(Point, 4326). Reused across all methods — no need to
    // instantiate a new one per call.
    private static final GeometryFactory GEOMETRY_FACTORY =
            new GeometryFactory(new PrecisionModel(), 4326);

    @Transactional
    public LocationTracking updateLocation(LocationUpdateDTO dto) {
        VisitorSession session = sessionRepository.findById(dto.getSessionId())
                .orElseThrow(() -> new DuplicateResourceException("Session not found"));

        LocationTracking location = new LocationTracking();
        location.setSession(session);
        location.setLatitude(dto.getLatitude());
        location.setLongitude(dto.getLongitude());
        location.setAccuracy(dto.getAccuracy());
        location.setBatteryLevel(dto.getBatteryLevel());
        // FIX: timestamp is set by @PrePersist in LocationTracking entity —
        // remove manual setTimestamp() call to avoid overwriting it

        // FIX: build a proper JTS Point instead of a WKT String.
        // Coordinate order is (longitude, latitude) — matches PostGIS convention
        // ST_MakePoint(longitude, latitude).
        Point point = GEOMETRY_FACTORY.createPoint(
                new Coordinate(dto.getLongitude(), dto.getLatitude())
        );
        session.setLastKnownLocation(point);
        session.setLastLocationUpdate(LocalDateTime.now());
        sessionRepository.save(session);

        return locationRepository.save(location);
    }

    public LocationTracking getLastLocation(Long sessionId) {
        VisitorSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new DuplicateResourceException("Session not found"));
        return locationRepository.findTopBySessionOrderByTimestampDesc(session);
    }

    public List<LocationTracking> getLocationHistory(
            Long sessionId, LocalDateTime from, LocalDateTime to) {

        VisitorSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new DuplicateResourceException("Session not found"));

        // FIX: use a repository query with time range instead of loading all
        // records and filtering in memory — avoids pulling the entire history
        // into the JVM just to discard most of it
        return locationRepository.findBySessionAndTimestampBetweenOrderByTimestampDesc(
                session, from, to);
    }
}