package com.vistara.tourist_tracking_system.service;

import com.vistara.tourist_tracking_system.dto.GeofenceZoneRequest;
import com.vistara.tourist_tracking_system.dto.GeofenceZoneResponse;
import com.vistara.tourist_tracking_system.model.GeofenceZone;
import com.vistara.tourist_tracking_system.repository.GeofenceZoneRepository;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.WKTReader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GeofenceZoneService {

    private final GeofenceZoneRepository geofenceZoneRepository;
    private final GeometryFactory geometryFactory = new GeometryFactory();
    private final WKTReader wktReader = new WKTReader(geometryFactory);

    @Transactional
    public GeofenceZoneResponse createZone(GeofenceZoneRequest request, Long adminUserId) {
        GeofenceZone zone = new GeofenceZone();
        zone.setZoneName(request.getZoneName());
        zone.setZoneDescription(request.getZoneDescription());
        zone.setZoneType(request.getZoneType());

        // Parse WKT to JTS Geometry
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
        return convertToResponse(updated);
    }

    @Transactional
    public void deleteZone(Long id) {
        GeofenceZone zone = geofenceZoneRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Zone not found"));
        zone.setIsActive(false);  // Soft delete
        geofenceZoneRepository.save(zone);
    }

    @Transactional(readOnly = true)
    public List<GeofenceZoneResponse> getZonesByType(GeofenceZone.ZoneType type) {
        return geofenceZoneRepository.findActiveByType(type)
                .stream()
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
}