package com.vistara.tourist_tracking_system.service;

import com.vistara.tourist_tracking_system.dto.BookingRequest;
import com.vistara.tourist_tracking_system.dto.AdminMpesaBookingRequest;
import com.vistara.tourist_tracking_system.dto.MpesaStkRequest;
import com.vistara.tourist_tracking_system.dto.MpesaStkResponse;
import com.vistara.tourist_tracking_system.model.Booking;
import com.vistara.tourist_tracking_system.model.User;
import com.vistara.tourist_tracking_system.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final MpesaService mpesaService;
    private final NotificationService notificationService;

    @Transactional
    public Booking createBooking(User tourist, BookingRequest request) {
        if (request.getCheckInDate().isBefore(LocalDate.now())) {
            throw new RuntimeException("Check‑in date cannot be in the past");
        }
        if (request.getCheckOutDate().isBefore(request.getCheckInDate())) {
            throw new RuntimeException("Check‑out date must be after check‑in date");
        }

        Booking booking = new Booking();
        booking.setUser(tourist);
        booking.setCheckInDate(request.getCheckInDate());
        booking.setCheckOutDate(request.getCheckOutDate());
        booking.setGroupSize(request.getGroupSize());
        booking.setVehicleRegistration(request.getVehicleRegistration());
        booking.setPaymentMethod(request.getPaymentMethod());
        booking.setAmount(request.getAmount());
        booking.setPaymentStatus(Booking.PaymentStatus.PENDING);
        booking.setBookingStatus(Booking.BookingStatus.PENDING);
        booking.setBookingReference(generateBookingReference());
        booking.setCheckinStatus(false);

        Booking saved = bookingRepository.save(booking);

        notificationService.createNotification(
                tourist,
                "Booking Created",
                "Your booking " + saved.getBookingReference() + " has been created. Please complete payment.",
                "BOOKING",
                saved.getId(),
                false
        );

        notificationService.createNotificationByEmail(
                "admin@vistara.com",
                "New Booking Created",
                "User " + tourist.getFullName() + " created a booking: " + saved.getBookingReference(),
                "BOOKING",
                saved.getId(),
                false
        );

        log.info("Booking created: {} for user {}", saved.getBookingReference(), tourist.getEmail());
        return saved;
    }

    /**
     * ✅ Generate a shorter booking reference compatible with M-Pesa
     * Format: VST + YYMMDD + HHMM + 4-digit random (max 16 characters)
     * Example: VST24070111569399
     */
    private String generateBookingReference() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmm"));
        String randomPart = String.format("%04d", (int) (Math.random() * 10000));
        return String.format("VST%s%s", datePart, randomPart);
    }

    @Transactional
    public void confirmPayment(Long bookingId, String paymentReference, String paymentStatus) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if ("PAID".equals(booking.getPaymentStatus())) {
            throw new RuntimeException("Booking is already PAID");
        }
        if ("CONFIRMED".equals(booking.getBookingStatus())) {
            throw new RuntimeException("Booking is already CONFIRMED");
        }

        booking.setPaymentReference(paymentReference);
        booking.setPaymentStatus(paymentStatus);

        if (Booking.PaymentStatus.PAID.equals(paymentStatus)) {
            booking.setBookingStatus(Booking.BookingStatus.CONFIRMED);

            // Send notification to user about manual payment confirmation
            notificationService.createNotification(
                    booking.getUser(),
                    "Payment Confirmed ✅",
                    "Your payment for booking " + booking.getBookingReference() + " has been confirmed by admin.",
                    "PAYMENT",
                    booking.getId(),
                    false
            );

            log.info("✅ Booking {} manually confirmed by admin", bookingId);
        }

        bookingRepository.save(booking);
        log.info("Payment confirmed for booking {} with reference {}", bookingId, paymentReference);
    }

    @Transactional
    public void cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        if (Booking.BookingStatus.CONFIRMED.equals(booking.getBookingStatus())) {
            throw new RuntimeException("Cannot cancel a confirmed booking; contact admin.");
        }
        booking.setBookingStatus(Booking.BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        notificationService.createNotification(
                booking.getUser(),
                "Booking Cancelled",
                "Your booking " + booking.getBookingReference() + " has been cancelled.",
                "BOOKING",
                bookingId,
                false
        );
    }

    @Transactional
    public void deleteBooking(Long bookingId, User currentUser) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (!Booking.PaymentStatus.PENDING.equals(booking.getPaymentStatus()) ||
                !Booking.BookingStatus.PENDING.equals(booking.getBookingStatus())) {
            throw new RuntimeException("Cannot delete booking – only pending bookings can be deleted");
        }

        boolean isAdmin = currentUser.getRole() == User.Role.ADMIN;
        boolean isOwner = booking.getUser().getId().equals(currentUser.getId());
        if (!isAdmin && !isOwner) {
            throw new RuntimeException("You are not authorized to delete this booking");
        }

        bookingRepository.delete(booking);
        log.info("Booking {} deleted by user {}", bookingId, currentUser.getEmail());
    }

    @Transactional
    public Booking createConfirmedBooking(User user, LocalDate checkInDate, LocalDate checkOutDate,
                                          Integer groupSize, String vehicleRegistration,
                                          BigDecimal amount, String paymentMethod, String adminNotes) {
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setCheckInDate(checkInDate);
        booking.setCheckOutDate(checkOutDate);
        booking.setGroupSize(groupSize);
        booking.setVehicleRegistration(vehicleRegistration);
        booking.setPaymentMethod(paymentMethod);
        booking.setAmount(amount);
        booking.setPaymentStatus(Booking.PaymentStatus.PAID);
        booking.setBookingStatus(Booking.BookingStatus.CONFIRMED);
        booking.setBookingReference(generateBookingReference());
        booking.setAdminNotes(adminNotes);
        booking.setCheckinStatus(false);

        Booking saved = bookingRepository.save(booking);

        notificationService.createNotification(
                user,
                "Booking Confirmed",
                "Your booking " + saved.getBookingReference() + " has been confirmed (CASH payment).",
                "BOOKING",
                saved.getId(),
                false
        );

        log.info("Confirmed booking created: {} for user {}", saved.getBookingReference(), user.getEmail());
        return saved;
    }

    @Transactional
    public Booking createBookingWithMpesa(User tourist, AdminMpesaBookingRequest request) {
        if (request.getCheckInDate().isBefore(LocalDate.now())) {
            throw new RuntimeException("Check‑in date cannot be in the past");
        }
        if (request.getCheckOutDate().isBefore(request.getCheckInDate())) {
            throw new RuntimeException("Check‑out date must be after check‑in date");
        }

        Booking booking = new Booking();
        booking.setUser(tourist);
        booking.setCheckInDate(request.getCheckInDate());
        booking.setCheckOutDate(request.getCheckOutDate());
        booking.setGroupSize(request.getNumberOfPeople());
        booking.setVehicleRegistration(request.getVehicleRegistration());
        booking.setPaymentMethod("MPESA");
        booking.setAmount(request.getAmount());
        booking.setPaymentStatus(Booking.PaymentStatus.PENDING);
        booking.setBookingStatus(Booking.BookingStatus.PENDING);
        booking.setBookingReference(generateBookingReference());
        booking.setAdminNotes(request.getNotes());
        booking.setCheckinStatus(false);

        Booking saved = bookingRepository.save(booking);

        notificationService.createNotification(
                tourist,
                "Booking Created (M-Pesa)",
                "Your booking " + saved.getBookingReference() + " has been created. Check your phone for M-Pesa prompt.",
                "BOOKING",
                saved.getId(),
                false
        );

        try {
            MpesaStkRequest mpesaRequest = new MpesaStkRequest();
            mpesaRequest.setPhoneNumber(request.getPhoneNumber());
            mpesaRequest.setAmount(request.getAmount().intValue());
            mpesaRequest.setAccountReference(saved.getBookingReference());
            mpesaRequest.setTransactionDesc("Vistara Park Entry Payment");

            MpesaStkResponse stkResponse = mpesaService.stkPush(mpesaRequest);
            saved.setPaymentTrackingId(stkResponse.getCheckoutRequestId());
            bookingRepository.save(saved);

            log.info("M-Pesa STK Push initiated for booking: {}", saved.getBookingReference());
        } catch (Exception e) {
            log.error("Failed to initiate M-Pesa payment for booking {}: {}", saved.getBookingReference(), e.getMessage());
            throw new RuntimeException("Failed to initiate M-Pesa payment: " + e.getMessage());
        }

        return saved;
    }

    public Booking getBookingById(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + bookingId));
    }

    public Booking getBookingByIdAndUser(Long bookingId, User user) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + bookingId));

        if (!booking.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You are not authorized to view this booking");
        }

        return booking;
    }

    /**
     * ✅ Find booking by reference with support for both full and partial matches
     */
    public Booking findByBookingReference(String bookingReference) {
        if (bookingReference == null || bookingReference.isEmpty()) {
            return null;
        }

        // Try exact match first
        Optional<Booking> booking = bookingRepository.findByBookingReference(bookingReference);
        if (booking.isPresent()) {
            log.info("Found booking by exact reference: {}", bookingReference);
            return booking.get();
        }

        // If not found, try to find by partial match (for truncated references)
        log.info("No exact match for reference: {}. Trying partial match...", bookingReference);
        List<Booking> allBookings = bookingRepository.findAll();
        Booking partialMatch = allBookings.stream()
                .filter(b -> b.getBookingReference().startsWith(bookingReference) ||
                        b.getBookingReference().contains(bookingReference))
                .findFirst()
                .orElse(null);

        if (partialMatch != null) {
            log.info("Found booking by partial match: {} -> {}", bookingReference, partialMatch.getBookingReference());
        } else {
            log.warn("No booking found for reference: {}", bookingReference);
        }

        return partialMatch;
    }

    public Booking findByPaymentTrackingId(String paymentTrackingId) {
        return bookingRepository.findByPaymentTrackingId(paymentTrackingId)
                .orElse(null);
    }

    @Transactional
    public void updatePaymentStatus(String checkoutRequestId, String paymentReference, String paymentStatus) {
        Booking booking = bookingRepository.findByPaymentTrackingId(checkoutRequestId)
                .orElseThrow(() -> new RuntimeException("Booking not found for CheckoutRequestID: " + checkoutRequestId));

        booking.setPaymentReference(paymentReference);
        booking.setPaymentStatus(paymentStatus);

        if ("PAID".equals(paymentStatus)) {
            booking.setBookingStatus(Booking.BookingStatus.CONFIRMED);

            // Send notification to user about successful payment
            notificationService.createNotification(
                    booking.getUser(),
                    "Payment Confirmed ✅",
                    "Your payment for booking " + booking.getBookingReference() + " has been confirmed. Your booking is now confirmed.",
                    "PAYMENT",
                    booking.getId(),
                    false
            );

            // Notify admin about successful payment
            notificationService.createNotificationByEmail(
                    "admin@vistara.com",
                    "Payment Confirmed",
                    "Booking " + booking.getBookingReference() + " has been paid and confirmed.",
                    "PAYMENT",
                    booking.getId(),
                    false
            );

            log.info("✅ Booking {} updated to PAID and notifications sent", booking.getId());
        } else if ("FAILED".equals(paymentStatus)) {
            // Send notification to user about failed payment
            notificationService.createNotification(
                    booking.getUser(),
                    "Payment Failed ❌",
                    "Your payment for booking " + booking.getBookingReference() + " has failed. Please try again or contact support.",
                    "PAYMENT",
                    booking.getId(),
                    false
            );

            log.warn("❌ Booking {} payment failed", booking.getId());
        }

        bookingRepository.save(booking);
        log.info("Booking {} updated to status: {}", booking.getId(), paymentStatus);
    }

    @Transactional
    public void updatePaymentTrackingId(Long bookingId, String trackingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        booking.setPaymentTrackingId(trackingId);
        bookingRepository.save(booking);
        log.info("Updated payment tracking ID for booking {}: {}", bookingId, trackingId);
    }

    public List<Booking> getBookingsByUser(User user) {
        return bookingRepository.findByUserId(user.getId());
    }

    public List<Booking> getAllBookings() {
        return bookingRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<Booking> getBookingsByStatus(String status) {
        return bookingRepository.findByBookingStatus(status);
    }

    /**
     * ✅ NEW: Save booking (used in check-in/checkout)
     */
    @Transactional
    public Booking saveBooking(Booking booking) {
        return bookingRepository.save(booking);
    }

    /**
     * ✅ NEW: Update check-in status only
     */
    @Transactional
    public void updateCheckinStatus(Long bookingId, boolean status) {
        Booking booking = getBookingById(bookingId);
        booking.setCheckinStatus(status);
        bookingRepository.save(booking);
        log.info("Booking {} check-in status updated to: {}", bookingId, status);
    }

    /**
     * Find active booking for a user (confirmed and not checked in)
     */
    public Booking findActiveBookingByUser(User user) {
        List<Booking> bookings = bookingRepository.findByUserId(user.getId());
        return bookings.stream()
                .filter(b -> Booking.BookingStatus.CONFIRMED.equals(b.getBookingStatus()))
                .filter(b -> !Boolean.TRUE.equals(b.getCheckinStatus()))
                .findFirst()
                .orElse(null);
    }
}