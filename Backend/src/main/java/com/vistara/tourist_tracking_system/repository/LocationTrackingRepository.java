package com.vistara.tourist_tracking_system.repository;

import com.vistara.tourist_tracking_system.model.LocationTracking;
import com.vistara.tourist_tracking_system.model.VisitorSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LocationTrackingRepository extends JpaRepository<LocationTracking, Long> {

    // Find the most recent location for a session
    LocationTracking findTopBySessionOrderByTimestampDesc(VisitorSession session);

    // Find all locations for a session between two dates
    List<LocationTracking> findBySessionAndTimestampBetween(VisitorSession session, LocalDateTime from, LocalDateTime to);

    // Find locations for a session ordered by timestamp
    List<LocationTracking> findBySessionOrderByTimestampDesc(VisitorSession session);

    // Find recent locations for all active sessions (for admin dashboard)
    @Query("SELECT lt FROM LocationTracking lt WHERE lt.timestamp > :since AND lt.session.active = true ORDER BY lt.timestamp DESC")
    List<LocationTracking> findRecentLocations(@Param("since") LocalDateTime since);

    // Find locations within a bounding box (using PostGIS)
    @Query(value = "SELECT * FROM location_tracking lt " +
            "WHERE ST_Within(location_point, ST_MakeEnvelope(:minLon, :minLat, :maxLon, :maxLat, 4326)) " +
            "ORDER BY timestamp DESC", nativeQuery = true)
    List<LocationTracking> findLocationsInBoundingBox(
            @Param("minLat") double minLat,
            @Param("minLon") double minLon,
            @Param("maxLat") double maxLat,
            @Param("maxLon") double maxLon
    );

    // Live tracking data for all active visitors
    @Query(value = "SELECT vs.id, u.full_name, u.email, lt.latitude, lt.longitude, lt.timestamp, " +
            "vs.group_size, vs.vehicle_registration, vs.sos_triggered " +
            "FROM visitor_sessions vs " +
            "JOIN users u ON vs.user_id = u.id " +
            "LEFT JOIN location_tracking lt ON lt.session_id = vs.id " +
            "WHERE vs.is_active = true " +
            "AND lt.timestamp IN (SELECT MAX(timestamp) FROM location_tracking WHERE session_id = vs.id) " +
            "ORDER BY lt.timestamp DESC", nativeQuery = true)
    List<Object[]> findLiveTrackingData();
}