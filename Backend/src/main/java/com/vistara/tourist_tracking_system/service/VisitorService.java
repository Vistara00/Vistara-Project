package com.vistara.tourist_tracking_system.service;

import com.vistara.tourist_tracking_system.dto.ActiveSessionResponse;
import com.vistara.tourist_tracking_system.dto.VisitorSessionResponse;
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
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class VisitorService {

    private final VisitorSessionRepository sessionRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Transactional
    public VisitorSession checkInByAdmin(Long bookingId, Long walkInUserId, String vehicleRegistrationOverride, String notes, User admin) {
        VisitorSession session = new VisitorSession();
        User tourist;

        if (bookingId != null && bookingId > 0) {
            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new RuntimeException("Booking not found"));
            if (!Booking.BookingStatus.CONFIRMED.equals(booking.getBookingStatus()) ||
                    !Booking.PaymentStatus.PAID.equals(booking.getPaymentStatus())) {
                throw new RuntimeException("Booking is not confirmed or payment not completed");
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

        // Check if user already has an active session
        List<VisitorSession> existingActiveSessions = sessionRepository.findByUserAndActiveTrue(tourist);
        if (!existingActiveSessions.isEmpty()) {
            for (VisitorSession existingSession : existingActiveSessions) {
                existingSession.setActive(false);
                existingSession.setCheckOutTime(LocalDateTime.now());
                existingSession.setNotes((existingSession.getNotes() != null ? existingSession.getNotes() + " | " : "") +
                        "Auto-closed due to new check-in");
                sessionRepository.save(existingSession);
                log.info("Auto-closed existing session {} for user {}", existingSession.getId(), tourist.getEmail());
            }
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

        if (tourist != null) {
            notificationService.createNotification(
                    tourist,
                    "Check-out Successful",
                    "You have been checked out of the park. Thank you for visiting!",
                    "CHECKOUT",
                    saved.getId(),
                    false
            );

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

    /**
     * Find the active session for a user.
     * Returns the most recent active session if multiple exist.
     */
    public VisitorSession findActiveSession(User user) {
        List<VisitorSession> sessions = sessionRepository.findByUserAndActiveTrue(user);

        if (sessions.isEmpty()) {
            return null;
        }

        // Return the most recent active session (latest check-in time)
        return sessions.stream()
                .max((s1, s2) -> s1.getCheckInTime().compareTo(s2.getCheckInTime()))
                .orElse(null);
    }

    /**
     * Convert VisitorSession to ActiveSessionResponse DTO
     * This limits the information exposed to the mobile app
     */
    public ActiveSessionResponse convertToActiveSessionResponse(VisitorSession session) {
        if (session == null) {
            return null;
        }

        ActiveSessionResponse response = new ActiveSessionResponse();

        // Session details
        response.setSessionId(session.getId());
        response.setCheckInTime(session.getCheckInTime());
        response.setCheckOutTime(session.getCheckOutTime());
        response.setActive(session.isActive());
        response.setGroupSize(session.getGroupSize());
        response.setVehicleRegistration(session.getVehicleRegistration());
        response.setSosTriggered(session.isSosTriggered());
        response.setHasEmergency(session.isHasEmergency());
        response.setNotes(session.getNotes());

        // Visitor details (limited - no password, no authorities)
        if (session.getUser() != null) {
            User user = session.getUser();
            ActiveSessionResponse.VisitorInfo visitor = new ActiveSessionResponse.VisitorInfo();
            visitor.setId(user.getId());
            visitor.setFullName(user.getFullName());
            visitor.setEmail(user.getEmail());
            visitor.setPhoneNumber(user.getPhoneNumber());
            visitor.setEmergencyContactName(user.getEmergencyContactName());
            visitor.setEmergencyContactPhone(user.getEmergencyContactPhone());
            response.setVisitor(visitor);
        }

        // Booking details (limited)
        if (session.getBooking() != null) {
            Booking booking = session.getBooking();
            ActiveSessionResponse.BookingInfo bookingInfo = new ActiveSessionResponse.BookingInfo();
            bookingInfo.setId(booking.getId());
            bookingInfo.setBookingReference(booking.getBookingReference());
            bookingInfo.setCheckInDate(booking.getCheckInDate());
            bookingInfo.setCheckOutDate(booking.getCheckOutDate());
            bookingInfo.setPaymentMethod(booking.getPaymentMethod());
            bookingInfo.setPaymentStatus(booking.getPaymentStatus());
            bookingInfo.setBookingStatus(booking.getBookingStatus());
            bookingInfo.setAmount(booking.getAmount());
            response.setBooking(bookingInfo);
        }

        return response;
    }

    /**
     * ✅ Convert VisitorSession to VisitorSessionResponse DTO
     * This is the method that was missing the fields
     */
    public VisitorSessionResponse convertToSessionResponse(VisitorSession session) {
        if (session == null) {
            return null;
        }

        VisitorSessionResponse response = new VisitorSessionResponse();

        // Session details
        response.setSessionId(session.getId());
        response.setCheckInTime(session.getCheckInTime());
        response.setCheckOutTime(session.getCheckOutTime());
        response.setActive(session.isActive());
        response.setGroupSize(session.getGroupSize());
        response.setVehicleRegistration(session.getVehicleRegistration());
        response.setSosTriggered(session.isSosTriggered());
        response.setHasEmergency(session.isHasEmergency());
        response.setNotes(session.getNotes());

        // Visitor details
        if (session.getUser() != null) {
            response.setUserId(session.getUser().getId());
            response.setVisitorName(session.getUser().getFullName());
            response.setVisitorEmail(session.getUser().getEmail());
            response.setVisitorPhone(session.getUser().getPhoneNumber());
        }

        // Booking details
        if (session.getBooking() != null) {
            response.setBookingId(session.getBooking().getId());
            response.setBookingReference(session.getBooking().getBookingReference());
            response.setBookingStatus(session.getBooking().getBookingStatus());
            response.setPaymentStatus(session.getBooking().getPaymentStatus());
        }

        return response;
    }
}