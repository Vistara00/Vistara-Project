package com.vistara.tourist_tracking_system.service;

import com.vistara.tourist_tracking_system.dto.BookingRequest;
import com.vistara.tourist_tracking_system.dto.MpesaStkRequest;
import com.vistara.tourist_tracking_system.dto.MpesaStkResponse;
import com.vistara.tourist_tracking_system.model.Booking;
import com.vistara.tourist_tracking_system.model.User;
import com.vistara.tourist_tracking_system.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;           // <-- ADD THIS IMPORT
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final MpesaService mpesaService;

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

        String ref = generateBookingReference();
        booking.setBookingReference(ref);

        Booking saved = bookingRepository.save(booking);

        if ("MPESA".equalsIgnoreCase(request.getPaymentMethod())) {
            MpesaStkRequest mpesaRequest = new MpesaStkRequest();
            mpesaRequest.setPhoneNumber(tourist.getPhoneNumber());
            mpesaRequest.setAmount(request.getAmount().intValue());
            mpesaRequest.setAccountReference(saved.getBookingReference());
            mpesaRequest.setTransactionDesc("Vistara Park Entry Payment");

            try {
                MpesaStkResponse stkResponse = mpesaService.stkPush(mpesaRequest);
                saved.setPaymentTrackingId(stkResponse.getCheckoutRequestId());
                bookingRepository.save(saved);
            } catch (Exception e) {
                throw new RuntimeException("Failed to initiate M-Pesa payment: " + e.getMessage());
            }
        }

        return saved;
    }

    private String generateBookingReference() {
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long count = bookingRepository.count() + 1;
        return String.format("VST-%s-%04d", datePart, count);
    }

    @Transactional
    public void confirmPayment(Long bookingId, String paymentReference, String paymentStatus) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        booking.setPaymentReference(paymentReference);
        booking.setPaymentStatus(paymentStatus);
        if (Booking.PaymentStatus.PAID.equals(paymentStatus)) {
            booking.setBookingStatus(Booking.BookingStatus.CONFIRMED);
        }
        bookingRepository.save(booking);
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
        return bookingRepository.save(booking);
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