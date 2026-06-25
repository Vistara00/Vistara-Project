package com.vistara.tourist_tracking_system.service;

import com.vistara.tourist_tracking_system.dto.LocationUpdateDTO;
import com.vistara.tourist_tracking_system.model.GeofenceZone;
import com.vistara.tourist_tracking_system.model.LocationTracking;
import com.vistara.tourist_tracking_system.model.VisitorSession;
import com.vistara.tourist_tracking_system.repository.GeofenceZoneRepository;
import com.vistara.tourist_tracking_system.repository.LocationTrackingRepository;
import com.vistara.tourist_tracking_system.repository.VisitorSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrackingService {

    private final LocationTrackingRepository locationRepository;
    private final VisitorSessionRepository sessionRepository;
    private final GeofenceZoneRepository geofenceZoneRepository;
    private final NotificationService notificationService;
    private final GeometryFactory geometryFactory = new GeometryFactory();

    /**
     * Update the visitor's location with geofence checking
     */
    @Transactional
    public LocationTracking updateLocation(LocationUpdateDTO dto) {
        // 1. Find the active session
        VisitorSession session = sessionRepository.findById(dto.getSessionId())
                .orElseThrow(() -> new RuntimeException("Session not found"));

        if (!session.isActive()) {
            throw new RuntimeException("Session is not active");
        }

        // 2. Create location point (PostGIS geometry) for session update
        Point locationPoint = geometryFactory.createPoint(
                new Coordinate(dto.getLongitude(), dto.getLatitude())
        );
        locationPoint.setSRID(4326);  // WGS84 coordinate system

        // 3. Check if visitor is within any geofence zone
        boolean isWithinGeofence = isWithinAnyGeofence(dto.getLatitude(), dto.getLongitude());

        // 4. Create LocationTracking entity
        LocationTracking location = new LocationTracking();
        location.setSession(session);
        location.setLatitude(dto.getLatitude());
        location.setLongitude(dto.getLongitude());
        location.setAccuracy(dto.getAccuracy());
        location.setBatteryLevel(dto.getBatteryLevel());
        location.setTimestamp(LocalDateTime.now());
        location.setWithinGeofence(isWithinGeofence);

        // 5. Update session's last known location
        session.setLastKnownLocation(locationPoint);
        session.setLastLocationUpdate(LocalDateTime.now());
        sessionRepository.save(session);

        // 6. Save the location tracking record
        LocationTracking saved = locationRepository.save(location);

        // 7. Check and notify about geofence zones
        checkAndNotifyGeofenceZones(session, dto.getLatitude(), dto.getLongitude());

        log.debug("Location updated for session {}: lat={}, lon={}, withinGeofence={}",
                session.getId(), dto.getLatitude(), dto.getLongitude(), isWithinGeofence);

        return saved;
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

    /**
     * Get all active visitors with their last known locations
     */
    public List<LocationTracking> getAllActiveVisitorsLocations() {
        return locationRepository.findRecentLocations(LocalDateTime.now().minusMinutes(5));
    }

    /**
     * Check if a location is within any geofence zone
     */
    public boolean isWithinAnyGeofence(double latitude, double longitude) {
        String pointWkt = String.format("POINT(%f %f)", longitude, latitude);
        List<GeofenceZone> zones = geofenceZoneRepository.findZonesContainingPoint(pointWkt);
        return !zones.isEmpty();
    }

    /**
     * Check if a location is within a specific geofence zone by type
     */
    public boolean isWithinGeofenceType(double latitude, double longitude, String zoneType) {
        String pointWkt = String.format("POINT(%f %f)", longitude, latitude);
        List<GeofenceZone> zones = geofenceZoneRepository.findZonesContainingPoint(pointWkt);
        return zones.stream().anyMatch(zone -> zone.getZoneType().name().equals(zoneType));
    }

    /**
     * Get all geofence zones containing a location
     */
    public List<GeofenceZone> getGeofenceZonesAtLocation(double latitude, double longitude) {
        String pointWkt = String.format("POINT(%f %f)", longitude, latitude);
        return geofenceZoneRepository.findZonesContainingPoint(pointWkt);
    }

    /**
     * Check and notify about geofence zones
     */
    @Transactional
    public void checkAndNotifyGeofenceZones(VisitorSession session, double latitude, double longitude) {
        List<GeofenceZone> zones = getGeofenceZonesAtLocation(latitude, longitude);

        if (zones.isEmpty()) {
            return;
        }

        for (GeofenceZone zone : zones) {
            // Check if visitor is entering a zone
            if (zone.getAlertOnEntry()) {
                String alertMessage = String.format(
                        "⚠️ You are entering %s: %s",
                        zone.getZoneName(),
                        zone.getZoneDescription() != null ? zone.getZoneDescription() : ""
                );

                // Notify the visitor
                notificationService.createNotification(
                        session.getUser(),
                        "Geofence Alert: " + zone.getZoneType().name(),
                        alertMessage,
                        "GEOFENCE",
                        zone.getId(),
                        false
                );

                // Notify admins/rangers about DANGER zones
                if (zone.getZoneType() == GeofenceZone.ZoneType.DANGER ||
                        zone.getZoneType() == GeofenceZone.ZoneType.WILDLIFE_AREA) {
                    notificationService.createNotificationByEmail(
                            "admin@vistara.com",
                            "⚠️ Visitor Entered " + zone.getZoneType().name() + " Zone",
                            "Visitor " + session.getUser().getFullName() +
                                    " entered " + zone.getZoneName() +
                                    " at " + latitude + ", " + longitude,
                            "GEOFENCE_ALERT",
                            zone.getId(),
                            false
                    );
                }

                log.info("Visitor {} entered geofence zone: {}", session.getUser().getEmail(), zone.getZoneName());
            }

            // Update visitor count for the zone
            if (zone.getCurrentVisitors() == null) {
                zone.setCurrentVisitors(0);
            }
            zone.setCurrentVisitors(zone.getCurrentVisitors() + 1);
            geofenceZoneRepository.save(zone);
        }
    }

    /**
     * Check if a visitor has exited a zone (called when location changes)
     * This compares previous location with current location to detect exits
     */
    @Transactional
    public void checkZoneExit(Long sessionId, double newLatitude, double newLongitude) {
        // Get the previous location
        LocationTracking previousLocation = locationRepository.findTopBySessionOrderByTimestampDesc(
                sessionRepository.findById(sessionId).orElse(null)
        );

        if (previousLocation == null) {
            return;
        }

        // Check if previous location was inside a zone and new location is outside
        String prevPointWkt = String.format("POINT(%f %f)", previousLocation.getLongitude(), previousLocation.getLatitude());
        String newPointWkt = String.format("POINT(%f %f)", newLongitude, newLatitude);

        List<GeofenceZone> previousZones = geofenceZoneRepository.findZonesContainingPoint(prevPointWkt);
        List<GeofenceZone> newZones = geofenceZoneRepository.findZonesContainingPoint(newPointWkt);

        // Find zones that were exited
        for (GeofenceZone zone : previousZones) {
            if (!newZones.contains(zone) && zone.getAlertOnExit()) {
                VisitorSession session = sessionRepository.findById(sessionId).orElse(null);
                if (session != null) {
                    notificationService.createNotification(
                            session.getUser(),
                            "Geofence Exit Alert",
                            "You have left " + zone.getZoneName(),
                            "GEOFENCE_EXIT",
                            zone.getId(),
                            false
                    );
                    log.info("Visitor {} exited geofence zone: {}", session.getUser().getEmail(), zone.getZoneName());
                }
            }
        }
    }
}