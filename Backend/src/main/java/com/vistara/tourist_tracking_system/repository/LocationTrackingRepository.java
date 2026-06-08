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

    // Unused warning — kept for future use, no code change needed
    List<LocationTracking> findBySessionOrderByTimestampDesc(VisitorSession session);

    LocationTracking findTopBySessionOrderByTimestampDesc(VisitorSession session);

    // FIX: was lt.session.isActive — JPQL uses the Java field name which is
    // "active" (renamed in VisitorSession to fix the Lombok boolean naming issue).
    // Also added @Param annotation to bind the :since parameter correctly.
    @Query("SELECT lt FROM LocationTracking lt WHERE lt.timestamp > :since AND lt.session.active = true")
    List<LocationTracking> findRecentLocations(@Param("since") LocalDateTime since);

    List<LocationTracking> findBySessionAndTimestampBetweenOrderByTimestampDesc(
            VisitorSession session, LocalDateTime from, LocalDateTime to);
}