package com.vistara.tourist_tracking_system.repository;

import com.vistara.tourist_tracking_system.model.GeofenceZone;
import com.vistara.tourist_tracking_system.model.GeofenceZone.ZoneType;
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

    @Query(value = "SELECT * FROM geofence_zones WHERE ST_Within(:point, zone_boundary) AND is_active = true", nativeQuery = true)
    List<GeofenceZone> findZonesContainingPoint(@Param("point") String pointWkt);

    @Query("SELECT gz FROM GeofenceZone gz WHERE gz.isActive = true AND gz.zoneType = :type")
    List<GeofenceZone> findActiveByType(@Param("type") ZoneType type);

    @Query(value = "SELECT * FROM geofence_zones WHERE ST_DWithin(center_point, :point, radius_meters) AND is_active = true", nativeQuery = true)
    List<GeofenceZone> findZonesWithinRadius(@Param("point") String pointWkt);

    // Count active zones
    long countByIsActiveTrue();

    // Count by zone type
    @Query("SELECT gz.zoneType, COUNT(gz) FROM GeofenceZone gz GROUP BY gz.zoneType")
    List<Object[]> countByZoneType();

    // Sum current visitors
    @Query("SELECT COALESCE(SUM(gz.currentVisitors), 0) FROM GeofenceZone gz WHERE gz.isActive = true")
    Integer sumCurrentVisitors();

    // Find zones near a point
    @Query(value = "SELECT * FROM geofence_zones gz WHERE gz.is_active = true " +
            "AND ST_DWithin(gz.zone_boundary, ST_SetSRID(ST_MakePoint(:lon, :lat), 4326), :radiusMeters)",
            nativeQuery = true)
    List<GeofenceZone> findZonesNearPoint(@Param("lat") double latitude,
                                          @Param("lon") double longitude,
                                          @Param("radiusMeters") double radiusMeters);
}