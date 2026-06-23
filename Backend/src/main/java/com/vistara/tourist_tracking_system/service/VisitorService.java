package com.vistara.tourist_tracking_system.service;

import com.vistara.tourist_tracking_system.model.Booking;
import com.vistara.tourist_tracking_system.model.User;
import com.vistara.tourist_tracking_system.model.VisitorSession;
import com.vistara.tourist_tracking_system.repository.BookingRepository;
import com.vistara.tourist_tracking_system.repository.UserRepository;
import com.vistara.tourist_tracking_system.repository.VisitorSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class VisitorService {

    private final VisitorSessionRepository sessionRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;  // Inject NotificationService

    @Transactional
    public VisitorSession checkInByAdmin(Long bookingId, Long walkInUserId, String vehicleRegistrationOverride, String notes, User admin) {
        VisitorSession session = new VisitorSession();
        User tourist;

        if (bookingId != null && bookingId > 0) {
            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new RuntimeException("Booking not found"));
            if (!Booking.BookingStatus.CONFIRMED.equals(booking.getBookingStatus())) {
                throw new RuntimeException("Booking is not confirmed (payment may be pending)");
            }
            tourist = booking.getUser();
            session.setBooking(booking);
            session.setGroupSize(booking.getGroupSize());
            session.setVehicleRegistration(vehicleRegistrationOverride != null ? vehicleRegistrationOverride : booking.getVehicleRegistration());
            log.info("Admin {} checked in booking {} for user {}", admin.getEmail(), bookingId, tourist.getEmail());
        } else if (walkInUserId != null && walkInUserId > 0) {
            tourist = userRepository.findById(walkInUserId)
                    .orElseThrow(() -> new RuntimeException("Walk‑in user not found"));
            session.setGroupSize(1);
            if (vehicleRegistrationOverride != null) {
                session.setVehicleRegistration(vehicleRegistrationOverride);
            }
            log.info("Admin {} checked in walk‑in user {}", admin.getEmail(), tourist.getEmail());
        } else {
            throw new RuntimeException("Either bookingId or walkInUserId must be provided for check‑in");
        }

        session.setUser(tourist);
        session.setCheckInTime(LocalDateTime.now());
        session.setActive(true);
        session.setNotes(notes);

        VisitorSession saved = sessionRepository.save(session);

        // Send notification to tourist about check-in
        if (tourist != null) {
            notificationService.createNotification(
                    tourist,
                    "Check-in Successful",
                    "You have been checked into the park. Session ID: " + saved.getId(),
                    "CHECKIN",
                    saved.getId(),
                    false
            );

            // Notify admin about successful check-in
            notificationService.createNotification(
                    admin,
                    "Check-in Completed",
                    "Visitor " + tourist.getFullName() + " has been checked in. Session ID: " + saved.getId(),
                    "CHECKIN",
                    saved.getId(),
                    false
            );

            log.info("Check-in notification sent to user: {}", tourist.getEmail());
        }

        return saved;
    }

    @Transactional
    public VisitorSession checkOutByAdmin(Long sessionId, String notes, User admin) {
        VisitorSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        if (!session.isActive()) {
            throw new RuntimeException("Session already checked out");
        }

        User tourist = session.getUser();

        session.setCheckOutTime(LocalDateTime.now());
        session.setActive(false);
        if (notes != null) {
            session.setNotes((session.getNotes() != null ? session.getNotes() + " | " : "") + notes);
        }

        if (session.getBooking() != null) {
            Booking booking = session.getBooking();
            booking.setBookingStatus(Booking.BookingStatus.COMPLETED);
            bookingRepository.save(booking);
            log.info("Admin {} completed booking {} for session {}", admin.getEmail(), booking.getId(), sessionId);
        }

        VisitorSession saved = sessionRepository.save(session);

        // Send notification to tourist about check-out
        if (tourist != null) {
            notificationService.createNotification(
                    tourist,
                    "Check-out Successful",
                    "You have been checked out of the park. Thank you for visiting!",
                    "CHECKOUT",
                    saved.getId(),
                    false
            );

            // Notify admin about successful check-out
            notificationService.createNotification(
                    admin,
                    "Check-out Completed",
                    "Visitor " + tourist.getFullName() + " has been checked out. Session ID: " + saved.getId(),
                    "CHECKOUT",
                    saved.getId(),
                    false
            );

            log.info("Check-out notification sent to user: {}", tourist.getEmail());
        }

        log.info("Admin {} checked out session {}", admin.getEmail(), sessionId);
        return saved;
    }

    public VisitorSession findActiveSession(User user) {
        return sessionRepository.findByUserAndActiveTrue(user)
                .orElse(null);
    }
}