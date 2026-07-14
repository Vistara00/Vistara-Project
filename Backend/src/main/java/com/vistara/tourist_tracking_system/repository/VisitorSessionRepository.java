package com.vistara.tourist_tracking_system.repository;

import com.vistara.tourist_tracking_system.model.User;
import com.vistara.tourist_tracking_system.model.VisitorSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VisitorSessionRepository extends JpaRepository<VisitorSession, Long> {

    // ===== Basic Queries =====

    List<VisitorSession> findByActiveTrue();

    List<VisitorSession> findByUserAndActiveTrue(User user);

    @Query("SELECT vs FROM VisitorSession vs WHERE vs.booking.id = :bookingId AND vs.active = true")
    VisitorSession findByBookingIdAndActiveTrue(@Param("bookingId") Long bookingId);

    @Query("SELECT vs FROM VisitorSession vs WHERE vs.user.id = :userId AND vs.active = true")
    VisitorSession findByUserIdAndActiveTrue(@Param("userId") Long userId);

    @Query("SELECT CASE WHEN COUNT(vs) > 0 THEN TRUE ELSE FALSE END FROM VisitorSession vs WHERE vs.user.id = :userId AND vs.active = true")
    boolean existsByUserIdAndActiveTrue(@Param("userId") Long userId);

    @Query("SELECT CASE WHEN COUNT(vs) > 0 THEN TRUE ELSE FALSE END FROM VisitorSession vs WHERE vs.booking.id = :bookingId AND vs.active = true")
    boolean existsByBookingIdAndActiveTrue(@Param("bookingId") Long bookingId);

    // ===== Dashboard Statistics =====

    @Query("SELECT COUNT(vs) FROM VisitorSession vs WHERE vs.active = true")
    long countByActiveTrue();

    @Query("SELECT COUNT(vs) FROM VisitorSession vs WHERE vs.checkInTime >= :startOfDay")
    long countCheckInsToday(@Param("startOfDay") LocalDateTime startOfDay);

    @Query("SELECT COUNT(vs) FROM VisitorSession vs WHERE vs.checkOutTime >= :startOfDay")
    long countCheckOutsToday(@Param("startOfDay") LocalDateTime startOfDay);

    /**
     * Daily attendance - using JPQL with DATE function
     */
    @Query("SELECT FUNCTION('DATE', vs.checkInTime) as date, COUNT(vs) as count " +
            "FROM VisitorSession vs " +
            "WHERE vs.checkInTime >= :startDate " +
            "GROUP BY FUNCTION('DATE', vs.checkInTime) " +
            "ORDER BY FUNCTION('DATE', vs.checkInTime) ASC")
    List<Object[]> findDailyAttendance(@Param("startDate") LocalDateTime startDate);

    /**
     * Weekly attendance - NATIVE SQL QUERY
     * ✅ FIXED: Using native SQL with EXTRACT(WEEK FROM ...)
     */
    @Query(value = "SELECT EXTRACT(WEEK FROM check_in_time) as weekNumber, COUNT(*) as count " +
            "FROM visitor_sessions " +
            "WHERE check_in_time >= :startDate " +
            "GROUP BY EXTRACT(WEEK FROM check_in_time) " +
            "ORDER BY EXTRACT(WEEK FROM check_in_time) ASC",
            nativeQuery = true)
    List<Object[]> findWeeklyAttendance(@Param("startDate") LocalDateTime startDate);

    // ===== Additional Methods =====

    @Query("SELECT vs FROM VisitorSession vs WHERE vs.active = true ORDER BY vs.checkInTime DESC")
    List<VisitorSession> findActiveSessionsWithDetails();

    @Query("SELECT vs FROM VisitorSession vs WHERE vs.checkInTime BETWEEN :startDate AND :endDate")
    List<VisitorSession> findByCheckInTimeBetween(@Param("startDate") LocalDateTime startDate,
                                                  @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(vs) FROM VisitorSession vs WHERE vs.user.id = :userId AND vs.active = true")
    long countActiveByUserId(@Param("userId") Long userId);

    @Query("SELECT vs FROM VisitorSession vs " +
            "LEFT JOIN FETCH vs.booking " +
            "LEFT JOIN FETCH vs.user " +
            "WHERE vs.active = true")
    List<VisitorSession> findAllActiveWithDetails();

    @Query("SELECT vs FROM VisitorSession vs WHERE vs.sosTriggered = true AND vs.active = true")
    List<VisitorSession> findActiveSosSessions();

    @Query("SELECT vs FROM VisitorSession vs WHERE vs.hasEmergency = true AND vs.active = true")
    List<VisitorSession> findActiveEmergencySessions();
}