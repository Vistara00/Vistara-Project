package com.vistara.tourist_tracking_system.repository;

import com.vistara.tourist_tracking_system.model.GeofenceZone;
import com.vistara.tourist_tracking_system.model.GeofenceZone.ZoneType;
import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GeofenceZoneRepository extends JpaRepository<GeofenceZone, Long> {

    List<GeofenceZone> findByIsActiveTrue();

    List<GeofenceZone> findByZoneType(ZoneType zoneType);

    Optional<GeofenceZone> findByZoneName(String zoneName);

    // ========== GEOFENCE QUERIES ==========

    // Find zones containing a point (using WKT string)
    @Query(value = "SELECT * FROM geofence_zones WHERE ST_Within(ST_GeomFromText(:point, 4326), zone_boundary) AND is_active = true", nativeQuery = true)
    List<GeofenceZone> findZonesContainingPoint(@Param("point") String pointWkt);

    // Find zones containing a point (using JTS Point)
    @Query(value = "SELECT * FROM geofence_zones WHERE ST_Within(:point, zone_boundary) AND is_active = true", nativeQuery = true)
    List<GeofenceZone> findZonesContainingPoint(@Param("point") Point point);

    // Find active zones by type
    @Query("SELECT gz FROM GeofenceZone gz WHERE gz.isActive = true AND gz.zoneType = :type")
    List<GeofenceZone> findActiveByType(@Param("type") ZoneType type);

    // Find zones near a point within radius
    @Query(value = "SELECT * FROM geofence_zones WHERE ST_DWithin(zone_boundary, ST_SetSRID(ST_MakePoint(:lon, :lat), 4326), :radiusMeters) AND is_active = true", nativeQuery = true)
    List<GeofenceZone> findZonesNearPoint(@Param("lat") double latitude,
                                          @Param("lon") double longitude,
                                          @Param("radiusMeters") double radiusMeters);

    // ========== STATISTICS QUERIES ==========

    // Count active zones
    @Query("SELECT COUNT(gz) FROM GeofenceZone gz WHERE gz.isActive = true")
    long countByIsActiveTrue();

    // Count by zone type
    @Query("SELECT gz.zoneType, COUNT(gz) FROM GeofenceZone gz GROUP BY gz.zoneType")
    List<Object[]> countByZoneType();

    // Sum current visitors in all active zones
    @Query("SELECT COALESCE(SUM(gz.currentVisitors), 0) FROM GeofenceZone gz WHERE gz.isActive = true")
    Integer sumCurrentVisitors();
}