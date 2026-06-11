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

    // ========== Admin check‑in (using a booking or walk‑in) ==========
    @Transactional
    public VisitorSession checkInByAdmin(Long bookingId, Long walkInUserId, String vehicleRegistrationOverride, String notes, User admin) {
        VisitorSession session = new VisitorSession();
        User tourist;

        if (bookingId != null && bookingId > 0) {
            // Check‑in using an existing confirmed booking
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
            // Walk‑in: use an existing user (tourist) directly – no prior booking
            tourist = userRepository.findById(walkInUserId)
                    .orElseThrow(() -> new RuntimeException("Walk‑in user not found"));
            session.setGroupSize(1); // default, can be overridden via notes
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

        return sessionRepository.save(session);
    }

    // ========== Admin check‑out ==========
    @Transactional
    public VisitorSession checkOutByAdmin(Long sessionId, String notes, User admin) {
        VisitorSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));
        if (!session.isActive()) {
            throw new RuntimeException("Session already checked out");
        }
        session.setCheckOutTime(LocalDateTime.now());
        session.setActive(false);
        if (notes != null) {
            session.setNotes((session.getNotes() != null ? session.getNotes() + " | " : "") + notes);
        }
        // If session has a booking, update its status to COMPLETED
        if (session.getBooking() != null) {
            Booking booking = session.getBooking();
            booking.setBookingStatus(Booking.BookingStatus.COMPLETED);
            bookingRepository.save(booking);
            log.info("Admin {} completed booking {} for session {}", admin.getEmail(), booking.getId(), sessionId);
        }
        log.info("Admin {} checked out session {}", admin.getEmail(), sessionId);
        return sessionRepository.save(session);
    }
}