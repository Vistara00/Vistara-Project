package com.vistara.tourist_tracking_system.service;

import com.vistara.tourist_tracking_system.dto.GeofenceZoneRequest;
import com.vistara.tourist_tracking_system.dto.GeofenceZoneResponse;
import com.vistara.tourist_tracking_system.model.GeofenceZone;
import com.vistara.tourist_tracking_system.model.VisitorSession;
import com.vistara.tourist_tracking_system.repository.GeofenceZoneRepository;
import com.vistara.tourist_tracking_system.repository.VisitorSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.WKTReader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeofenceZoneService {

    private final GeofenceZoneRepository geofenceZoneRepository;
    private final VisitorSessionRepository sessionRepository;
    private final NotificationService notificationService;
    private final GeometryFactory geometryFactory = new GeometryFactory();
    private final WKTReader wktReader = new WKTReader(geometryFactory);

    // ========== CORE CRUD OPERATIONS ==========

    @Transactional
    public GeofenceZoneResponse createZone(GeofenceZoneRequest request, Long adminUserId) {
        GeofenceZone zone = new GeofenceZone();
        zone.setZoneName(request.getZoneName());
        zone.setZoneDescription(request.getZoneDescription());
        zone.setZoneType(request.getZoneType());

        try {
            Polygon polygon = (Polygon) wktReader.read(request.getZoneBoundaryWkt());
            polygon.setSRID(4326);
            zone.setZoneBoundary(polygon);

            if (request.getCenterPointWkt() != null) {
                Point point = (Point) wktReader.read(request.getCenterPointWkt());
                point.setSRID(4326);
                zone.setCenterPoint(point);
            }
        } catch (Exception e) {
            throw new RuntimeException("Invalid WKT format: " + e.getMessage());
        }

        zone.setRadiusMeters(request.getRadiusMeters());
        zone.setAlertOnEntry(request.getAlertOnEntry());
        zone.setAlertOnExit(request.getAlertOnExit());
        zone.setRequiresEscort(request.getRequiresEscort());
        zone.setMaxVisitors(request.getMaxVisitors());
        zone.setIsActive(request.getIsActive());
        zone.setCreatedBy(adminUserId);

        GeofenceZone saved = geofenceZoneRepository.save(zone);
        log.info("Geofence zone created: {} ({})", saved.getZoneName(), saved.getZoneType());
        return convertToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<GeofenceZoneResponse> getAllActiveZones() {
        return geofenceZoneRepository.findByIsActiveTrue()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<GeofenceZoneResponse> getAllZones() {
        return geofenceZoneRepository.findAll()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public GeofenceZoneResponse getZoneById(Long id) {
        GeofenceZone zone = geofenceZoneRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Zone not found with id: " + id));
        return convertToResponse(zone);
    }

    @Transactional
    public GeofenceZoneResponse updateZone(Long id, GeofenceZoneRequest request, Long adminUserId) {
        GeofenceZone zone = geofenceZoneRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Zone not found"));

        zone.setZoneName(request.getZoneName());
        zone.setZoneDescription(request.getZoneDescription());
        zone.setZoneType(request.getZoneType());

        try {
            Polygon polygon = (Polygon) wktReader.read(request.getZoneBoundaryWkt());
            polygon.setSRID(4326);
            zone.setZoneBoundary(polygon);

            if (request.getCenterPointWkt() != null) {
                Point point = (Point) wktReader.read(request.getCenterPointWkt());
                point.setSRID(4326);
                zone.setCenterPoint(point);
            }
        } catch (Exception e) {
            throw new RuntimeException("Invalid WKT format: " + e.getMessage());
        }

        zone.setRadiusMeters(request.getRadiusMeters());
        zone.setAlertOnEntry(request.getAlertOnEntry());
        zone.setAlertOnExit(request.getAlertOnExit());
        zone.setRequiresEscort(request.getRequiresEscort());
        zone.setMaxVisitors(request.getMaxVisitors());
        zone.setIsActive(request.getIsActive());

        GeofenceZone updated = geofenceZoneRepository.save(zone);
        log.info("Geofence zone updated: {} ({})", updated.getZoneName(), updated.getZoneType());
        return convertToResponse(updated);
    }

    @Transactional
    public void deleteZone(Long id) {
        GeofenceZone zone = geofenceZoneRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Zone not found"));
        zone.setIsActive(false);
        geofenceZoneRepository.save(zone);
        log.info("Geofence zone deactivated: {}", zone.getZoneName());
    }

    @Transactional
    public GeofenceZoneResponse toggleZoneStatus(Long id) {
        GeofenceZone zone = geofenceZoneRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Zone not found"));
        zone.setIsActive(!zone.getIsActive());
        GeofenceZone saved = geofenceZoneRepository.save(zone);
        log.info("Zone {} status toggled to: {}", zone.getZoneName(), saved.getIsActive());
        return convertToResponse(saved);
    }

    // ========== QUERY OPERATIONS ==========

    @Transactional(readOnly = true)
    public List<GeofenceZoneResponse> getZonesByType(GeofenceZone.ZoneType type) {
        return geofenceZoneRepository.findActiveByType(type)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<GeofenceZoneResponse> getZonesWithVisitorCount() {
        List<GeofenceZone> zones = geofenceZoneRepository.findByIsActiveTrue();
        return zones.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<GeofenceZoneResponse> getZonesNearLocation(double latitude, double longitude, double radiusMeters) {
        List<GeofenceZone> zones = geofenceZoneRepository.findZonesNearPoint(latitude, longitude, radiusMeters);
        return zones.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public GeofenceZoneResponse checkVisitorLocation(double latitude, double longitude) {
        String pointWkt = String.format("POINT(%f %f)", longitude, latitude);
        List<GeofenceZone> zones = geofenceZoneRepository.findZonesContainingPoint(pointWkt);
        if (zones.isEmpty()) {
            return null;
        }
        return convertToResponse(zones.get(0));
    }

    @Transactional(readOnly = true)
    public List<GeofenceZoneResponse> getZonesContainingLocation(double latitude, double longitude) {
        String pointWkt = String.format("POINT(%f %f)", longitude, latitude);
        List<GeofenceZone> zones = geofenceZoneRepository.findZonesContainingPoint(pointWkt);
        return zones.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // ========== STATISTICS ==========

    @Transactional(readOnly = true)
    public ZoneStats getZoneStats() {
        ZoneStats stats = new ZoneStats();
        stats.setTotalZones(geofenceZoneRepository.count());
        stats.setActiveZones(geofenceZoneRepository.countByIsActiveTrue());

        // Convert List<Object[]> to Map<String, Long>
        List<Object[]> zoneTypeCounts = geofenceZoneRepository.countByZoneType();
        Map<String, Long> zonesByTypeMap = new HashMap<>();
        for (Object[] row : zoneTypeCounts) {
            String zoneType = row[0].toString();
            Long count = ((Number) row[1]).longValue();
            zonesByTypeMap.put(zoneType, count);
        }
        stats.setZonesByType(zonesByTypeMap);

        stats.setTotalVisitorsInZones(geofenceZoneRepository.sumCurrentVisitors());
        return stats;
    }

    // ========== GEOFENCE MONITORING ==========

    /**
     * Check if a visitor is within any geofence zone and trigger notifications
     */
    @Transactional
    public void checkAndNotifyVisitorZone(Long sessionId, double latitude, double longitude) {
        String pointWkt = String.format("POINT(%f %f)", longitude, latitude);
        List<GeofenceZone> zones = geofenceZoneRepository.findZonesContainingPoint(pointWkt);

        VisitorSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        if (zones.isEmpty()) {
            checkVisitorExit(sessionId, latitude, longitude);
            return;
        }

        for (GeofenceZone zone : zones) {
            if (zone.getAlertOnEntry()) {
                String message = String.format(
                        "⚠️ You are entering %s: %s",
                        zone.getZoneName(),
                        zone.getZoneDescription() != null ? zone.getZoneDescription() : ""
                );

                notificationService.createNotification(
                        session.getUser(),
                        "Geofence Alert: " + zone.getZoneType().name(),
                        message,
                        "GEOFENCE",
                        zone.getId(),
                        false
                );
                log.info("Visitor {} entered zone: {}", session.getUser().getEmail(), zone.getZoneName());

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
            }

            if (zone.getCurrentVisitors() == null) {
                zone.setCurrentVisitors(0);
            }
            zone.setCurrentVisitors(zone.getCurrentVisitors() + 1);
            geofenceZoneRepository.save(zone);
        }
    }

    /**
     * Check if a visitor has exited a zone
     */
    @Transactional
    public void checkVisitorExit(Long sessionId, double newLatitude, double newLongitude) {
        VisitorSession session = sessionRepository.findById(sessionId).orElse(null);
        if (session == null || session.getLastKnownLocation() == null) {
            return;
        }

        Point previousPoint = session.getLastKnownLocation();
        double prevLat = previousPoint.getY();
        double prevLon = previousPoint.getX();

        String prevPointWkt = String.format("POINT(%f %f)", prevLon, prevLat);
        String newPointWkt = String.format("POINT(%f %f)", newLongitude, newLatitude);

        List<GeofenceZone> previousZones = geofenceZoneRepository.findZonesContainingPoint(prevPointWkt);
        List<GeofenceZone> newZones = geofenceZoneRepository.findZonesContainingPoint(newPointWkt);

        for (GeofenceZone zone : previousZones) {
            if (!newZones.contains(zone) && zone.getAlertOnExit()) {
                notificationService.createNotification(
                        session.getUser(),
                        "Geofence Exit Alert",
                        "You have left " + zone.getZoneName(),
                        "GEOFENCE_EXIT",
                        zone.getId(),
                        false
                );
                log.info("Visitor {} exited geofence zone: {}", session.getUser().getEmail(), zone.getZoneName());

                if (zone.getCurrentVisitors() != null && zone.getCurrentVisitors() > 0) {
                    zone.setCurrentVisitors(zone.getCurrentVisitors() - 1);
                    geofenceZoneRepository.save(zone);
                }
            }
        }
    }

    // ========== UTILITY METHODS ==========

    @Transactional(readOnly = true)
    public boolean isWithinAnyGeofence(double latitude, double longitude) {
        String pointWkt = String.format("POINT(%f %f)", longitude, latitude);
        List<GeofenceZone> zones = geofenceZoneRepository.findZonesContainingPoint(pointWkt);
        return !zones.isEmpty();
    }

    @Transactional(readOnly = true)
    public boolean isWithinGeofenceType(double latitude, double longitude, String zoneType) {
        String pointWkt = String.format("POINT(%f %f)", longitude, latitude);
        List<GeofenceZone> zones = geofenceZoneRepository.findZonesContainingPoint(pointWkt);
        return zones.stream().anyMatch(zone -> zone.getZoneType().name().equals(zoneType));
    }

    // ========== PRIVATE HELPERS ==========

    private GeofenceZoneResponse convertToResponse(GeofenceZone zone) {
        GeofenceZoneResponse response = new GeofenceZoneResponse();
        response.setId(zone.getId());
        response.setZoneName(zone.getZoneName());
        response.setZoneDescription(zone.getZoneDescription());
        response.setZoneType(zone.getZoneType());

        if (zone.getZoneBoundary() != null) {
            response.setZoneBoundaryWkt(zone.getZoneBoundary().toText());
        }
        if (zone.getCenterPoint() != null) {
            response.setCenterPointWkt(zone.getCenterPoint().toText());
        }

        response.setRadiusMeters(zone.getRadiusMeters());
        response.setAlertOnEntry(zone.getAlertOnEntry());
        response.setAlertOnExit(zone.getAlertOnExit());
        response.setRequiresEscort(zone.getRequiresEscort());
        response.setMaxVisitors(zone.getMaxVisitors());
        response.setCurrentVisitors(zone.getCurrentVisitors());
        response.setIsActive(zone.getIsActive());
        response.setCreatedBy(zone.getCreatedBy());
        response.setCreatedAt(zone.getCreatedAt());
        response.setUpdatedAt(zone.getUpdatedAt());
        return response;
    }

    // ========== INNER CLASSES ==========

    @lombok.Data
    public static class ZoneStats {
        private long totalZones;
        private long activeZones;
        private Map<String, Long> zonesByType;
        private Integer totalVisitorsInZones;
    }
}