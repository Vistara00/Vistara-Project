package com.vistara.tourist_tracking_system.repository;

import com.vistara.tourist_tracking_system.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findByBookingReference(String bookingReference);

    List<Booking> findByUserId(Long userId);

    List<Booking> findByBookingStatus(String status);

    List<Booking> findByPaymentStatus(String paymentStatus);

    List<Booking> findByCheckInDateBetween(LocalDate start, LocalDate end);

    List<Booking> findAllByOrderByCreatedAtDesc();

    Optional<Booking> findByPaymentTrackingId(String paymentTrackingId);

    @Query("SELECT b FROM Booking b WHERE b.user.id = :userId AND b.bookingStatus = :status")
    List<Booking> findUserBookingsByStatus(@Param("userId") Long userId, @Param("status") String status);

    // Daily average revenue for last N days
    @Query("SELECT AVG(b.amount) FROM Booking b WHERE b.paymentStatus = 'PAID' AND b.updatedAt >= :startDate")
    Double getAverageDailyRevenue(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT FUNCTION('DATE', b.updatedAt) as date, SUM(b.amount) as total FROM Booking b " +
            "WHERE b.paymentStatus = 'PAID' AND b.updatedAt >= :startDate GROUP BY FUNCTION('DATE', b.updatedAt) ORDER BY date")
    List<Object[]> getDailyRevenue(@Param("startDate") LocalDateTime startDate);

    @Modifying
    @Query("UPDATE Booking b SET b.bookingStatus = :newStatus WHERE b.id = :bookingId AND b.bookingStatus = :currentStatus")
    int updateBookingStatus(@Param("bookingId") Long bookingId,
                            @Param("currentStatus") String currentStatus,
                            @Param("newStatus") String newStatus);
}