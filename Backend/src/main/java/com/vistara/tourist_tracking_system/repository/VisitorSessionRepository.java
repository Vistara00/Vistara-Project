package com.vistara.tourist_tracking_system.repository;

import com.vistara.tourist_tracking_system.model.VisitorSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VisitorSessionRepository extends JpaRepository<VisitorSession, Long> {

    // FIX: was findByIsActiveTrue() — field renamed to "active" in entity
    List<VisitorSession> findByActiveTrue();

    // FIX: replaces the non-existent findStaleSessions() — used for today's check-ins
    List<VisitorSession> findByCheckInTimeAfter(LocalDateTime since);
}