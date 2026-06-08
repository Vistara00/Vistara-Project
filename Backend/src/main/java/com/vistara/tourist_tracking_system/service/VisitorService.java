package com.vistara.tourist_tracking_system.service;

import com.vistara.tourist_tracking_system.dto.CheckInRequest;
import com.vistara.tourist_tracking_system.dto.CheckOutRequest;
import com.vistara.tourist_tracking_system.model.User;
import com.vistara.tourist_tracking_system.model.VisitorSession;
import com.vistara.tourist_tracking_system.repository.VisitorSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class VisitorService {

    private final VisitorSessionRepository sessionRepository;

    @Transactional
    public VisitorSession checkIn(User user, CheckInRequest request) {
        // Validate payment method
        VisitorSession.PaymentMethod method;
        try {
            method = VisitorSession.PaymentMethod.valueOf(request.getPaymentMethod().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid payment method. Use MPESA or E_CITIZEN");
        }

        VisitorSession session = new VisitorSession();
        session.setUser(user);
        session.setGroupSize(request.getGroupSize());
        session.setVehicleRegistration(request.getVehicleRegistration());
        session.setPaymentMethod(method);
        session.setAmount(request.getAmount());
        session.setPaymentReference(request.getPaymentReference());
        session.setBookingNotes(request.getBookingNotes());
        session.setIsPaid(true);   // Assuming payment completed; integrate M-Pesa API if needed

        // Explicitly set check-in time (will also be set by @PrePersist, but good to be explicit)
        session.setCheckInTime(LocalDateTime.now());

        return sessionRepository.save(session);
    }

    @Transactional
    public VisitorSession checkOut(CheckOutRequest request) {
        VisitorSession session = sessionRepository.findById(request.getSessionId())
                .orElseThrow(() -> new RuntimeException("Session not found with id: " + request.getSessionId()));

        if (!session.isActive()) {
            throw new RuntimeException("Session is already checked out");
        }

        // Set check-out time and deactivate
        session.setCheckOutTime(LocalDateTime.now());
        session.setActive(false);

        // Optional: add notes (e.g., "checked out early" or duration)
        if (request.getNotes() != null && !request.getNotes().isEmpty()) {
            String existingNotes = session.getNotes();
            session.setNotes((existingNotes != null ? existingNotes + " | " : "") + request.getNotes());
        }

        // Calculate visit duration (in minutes) and append to notes
        long minutes = Duration.between(session.getCheckInTime(), session.getCheckOutTime()).toMinutes();
        String durationNote = "Visit duration: " + minutes + " minutes";
        session.setNotes((session.getNotes() != null ? session.getNotes() + " | " : "") + durationNote);

        return sessionRepository.save(session);
    }
}