package com.vistara.tourist_tracking_system.repository;

import com.vistara.tourist_tracking_system.model.User;
import com.vistara.tourist_tracking_system.model.VisitorSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VisitorSessionRepository extends JpaRepository<VisitorSession, Long> {

    // Active sessions (list)
    List<VisitorSession> findByActiveTrue();

    // Count of active sessions (for dashboard)
    long countByActiveTrue();

    // Find active sessions for a specific user (returns list)
    List<VisitorSession> findByUserAndActiveTrue(User user);

    // Find latest active session for a user (returns single result)
    @Query("SELECT vs FROM VisitorSession vs WHERE vs.user = :user AND vs.active = true ORDER BY vs.checkInTime DESC")
    Optional<VisitorSession> findLatestActiveSessionByUser(@Param("user") User user);

    // Sessions checked-in after a given time
    List<VisitorSession> findByCheckInTimeAfter(LocalDateTime since);

    // Daily attendance queries
    @Query("SELECT COUNT(vs) FROM VisitorSession vs WHERE vs.checkInTime >= :startOfDay")
    long countCheckInsToday(@Param("startOfDay") LocalDateTime startOfDay);

    @Query("SELECT COUNT(vs) FROM VisitorSession vs WHERE vs.checkOutTime >= :startOfDay")
    long countCheckOutsToday(@Param("startOfDay") LocalDateTime startOfDay);

    @Query(value = "SELECT DATE(vs.check_in_time) as date, COUNT(vs.id) as count " +
            "FROM visitor_sessions vs " +
            "WHERE vs.check_in_time >= :startDate " +
            "GROUP BY DATE(vs.check_in_time) " +
            "ORDER BY DATE(vs.check_in_time)", nativeQuery = true)
    List<Object[]> findDailyAttendance(@Param("startDate") LocalDateTime startDate);

    @Query(value = "SELECT EXTRACT(WEEK FROM vs.check_in_time) as week, COUNT(vs.id) as count " +
            "FROM visitor_sessions vs " +
            "WHERE vs.check_in_time >= :startDate " +
            "GROUP BY EXTRACT(WEEK FROM vs.check_in_time) " +
            "ORDER BY EXTRACT(WEEK FROM vs.check_in_time)", nativeQuery = true)
    List<Object[]> findWeeklyAttendance(@Param("startDate") LocalDateTime startDate);

    List<VisitorSession> findByActiveTrueAndCheckOutTimeIsNull();

    List<VisitorSession> findByUserIdAndActiveTrue(Long userId);
}