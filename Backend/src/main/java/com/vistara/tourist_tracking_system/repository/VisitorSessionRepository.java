package com.vistara.tourist_tracking_system.repository;

import com.vistara.tourist_tracking_system.model.VisitorSession;
import com.vistara.tourist_tracking_system.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VisitorSessionRepository extends JpaRepository<VisitorSession, Long> {

    List<VisitorSession> findByUserAndIsActiveTrue(User user);

    List<VisitorSession> findByIsActiveTrue();

    @Query("SELECT vs FROM VisitorSession vs WHERE vs.isActive = true AND vs.checkInTime < :threshold")
    List<VisitorSession> findStaleSessions(LocalDateTime threshold);

//    Optional<VisitorSession> findByUserAndIsActiveTrue(User user);
}