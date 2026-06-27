package com.vistara.tourist_tracking_system.service;

import com.vistara.tourist_tracking_system.dto.BookingRequest;
import com.vistara.tourist_tracking_system.dto.AdminMpesaBookingRequest;
import com.vistara.tourist_tracking_system.dto.MpesaStkRequest;
import com.vistara.tourist_tracking_system.dto.MpesaStkResponse;
import com.vistara.tourist_tracking_system.model.Booking;
import com.vistara.tourist_tracking_system.model.User;
import com.vistara.tourist_tracking_system.model.Role;
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

    private String generateBookingReference() {
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String timePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss"));
        String randomPart = String.format("%04d", (int) (Math.random() * 10000));
        return String.format("VST-%s-%s-%s", datePart, timePart, randomPart);
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

        boolean isAdmin = currentUser.getRole() == Role.ADMIN;
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

    public Booking findByBookingReference(String bookingReference) {
        return bookingRepository.findByBookingReference(bookingReference)
                .orElse(null);
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
}