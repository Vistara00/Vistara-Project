package com.vistara.tourist_tracking_system.repository;

import com.vistara.tourist_tracking_system.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findByBookingReference(String bookingReference);

    Optional<Booking> findByPaymentTrackingId(String paymentTrackingId);

    List<Booking> findByUserId(Long userId);

    List<Booking> findByBookingStatus(String status);

    List<Booking> findAllByOrderByCreatedAtDesc();

    /**
     * Get daily revenue for the last N days
     */
    @Query("SELECT DATE(b.createdAt) as date, SUM(b.amount) as revenue " +
            "FROM Booking b " +
            "WHERE b.paymentStatus = 'PAID' AND b.createdAt >= :startDate " +
            "GROUP BY DATE(b.createdAt) " +
            "ORDER BY DATE(b.createdAt) ASC")
    List<Object[]> getDailyRevenue(@Param("startDate") LocalDateTime startDate);

    /**
     * Get total revenue by payment method
     */
    @Query("SELECT b.paymentMethod, SUM(b.amount) FROM Booking b " +
            "WHERE b.paymentStatus = 'PAID' " +
            "GROUP BY b.paymentMethod")
    List<Object[]> getRevenueByPaymentMethod();

    /**
     * Get booking stats by status
     */
    @Query("SELECT b.bookingStatus, COUNT(b) FROM Booking b GROUP BY b.bookingStatus")
    List<Object[]> countByBookingStatus();

    /**
     * Get payment stats by status
     */
    @Query("SELECT b.paymentStatus, COUNT(b) FROM Booking b GROUP BY b.paymentStatus")
    List<Object[]> countByPaymentStatus();

    /**
     * Count bookings created today
     */
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.createdAt >= :startOfDay")
    long countCreatedToday(@Param("startOfDay") LocalDateTime startOfDay);

    @Query("SELECT b FROM Booking b WHERE b.user.id = :userId AND b.bookingStatus = 'CONFIRMED' AND (b.checkinStatus IS NULL OR b.checkinStatus = false)")
    List<Booking> findActiveBookingsByUser(@Param("userId") Long userId);
}