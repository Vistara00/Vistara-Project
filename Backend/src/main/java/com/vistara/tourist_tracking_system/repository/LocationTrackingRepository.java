package com.vistara.tourist_tracking_system.repository;

import com.vistara.tourist_tracking_system.model.LocationTracking;
import com.vistara.tourist_tracking_system.model.VisitorSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LocationTrackingRepository extends JpaRepository<LocationTracking, Long> {

    List<LocationTracking> findBySessionOrderByTimestampDesc(VisitorSession session);

    LocationTracking findTopBySessionOrderByTimestampDesc(VisitorSession session);

    @Query("SELECT lt FROM LocationTracking lt WHERE lt.timestamp > :since AND lt.session.isActive = true")
    List<LocationTracking> findRecentLocations(LocalDateTime since);
}