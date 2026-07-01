package com.vistara.tourist_tracking_system.controller;

import com.vistara.tourist_tracking_system.dto.ApiResponse;
import com.vistara.tourist_tracking_system.dto.BookingRequest;
import com.vistara.tourist_tracking_system.dto.BookingResponse;
import com.vistara.tourist_tracking_system.dto.PaymentStatusResponse;
import com.vistara.tourist_tracking_system.model.Booking;
import com.vistara.tourist_tracking_system.model.User;
import com.vistara.tourist_tracking_system.service.BookingService;
import com.vistara.tourist_tracking_system.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<ApiResponse<BookingResponse>> createBooking(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody BookingRequest request) {

        User tourist = userService.findByEmail(userDetails.getUsername());
        Booking booking = bookingService.createBooking(tourist, request);
        BookingResponse response = convertToResponse(booking);
        return ResponseEntity.ok(ApiResponse.success(response, "Booking created. Complete payment to confirm."));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getUserBookings(
            @AuthenticationPrincipal UserDetails userDetails) {
        User tourist = userService.findByEmail(userDetails.getUsername());
        List<Booking> bookings = bookingService.getBookingsByUser(tourist);
        List<BookingResponse> responses = bookings.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(responses, "Your bookings retrieved"));
    }

    /**
     * Get a specific booking by ID (for the authenticated visitor)
     * Only returns the booking if it belongs to the authenticated user
     */
    @GetMapping("/{bookingId}")
    public ResponseEntity<ApiResponse<BookingResponse>> getBookingById(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long bookingId) {
        User user = userService.findByEmail(userDetails.getUsername());
        Booking booking = bookingService.getBookingByIdAndUser(bookingId, user);
        BookingResponse response = convertToResponse(booking);
        return ResponseEntity.ok(ApiResponse.success(response, "Booking retrieved successfully"));
    }

    /**
     * ✅ Get only the payment status of a booking
     * Returns just the payment status field
     */
    @GetMapping("/{bookingId}/payment-status")
    public ResponseEntity<ApiResponse<PaymentStatusResponse>> getPaymentStatus(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long bookingId) {
        User user = userService.findByEmail(userDetails.getUsername());
        Booking booking = bookingService.getBookingByIdAndUser(bookingId, user);

        PaymentStatusResponse response = new PaymentStatusResponse();
        response.setBookingId(booking.getId());
//        response.setBookingReference(booking.getBookingReference());
        response.setPaymentStatus(booking.getPaymentStatus());
//        response.setBookingStatus(booking.getBookingStatus());
//        response.setPaymentReference(booking.getPaymentReference());
//        response.setAmount(booking.getAmount());

        return ResponseEntity.ok(ApiResponse.success(response, "Payment status retrieved"));
    }

    /**
     * ✅ Get payment status by booking reference (public, no auth required)
     * Useful for checking payment status without authentication
     */
    @GetMapping("/payment-status/{bookingReference}")
    public ResponseEntity<ApiResponse<PaymentStatusResponse>> getPaymentStatusByReference(
            @PathVariable String bookingReference) {
        Booking booking = bookingService.findByBookingReference(bookingReference);

        if (booking == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Booking not found with reference: " + bookingReference));
        }

        PaymentStatusResponse response = new PaymentStatusResponse();
        response.setBookingId(booking.getId());
//        response.setBookingReference(booking.getBookingReference());
        response.setPaymentStatus(booking.getPaymentStatus());
//        response.setBookingStatus(booking.getBookingStatus());
//        response.setPaymentReference(booking.getPaymentReference());
//        response.setAmount(booking.getAmount());

        return ResponseEntity.ok(ApiResponse.success(response, "Payment status retrieved"));
    }

    @PostMapping("/{bookingId}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelBooking(@PathVariable Long bookingId) {
        bookingService.cancelBooking(bookingId);
        return ResponseEntity.ok(ApiResponse.success(null, "Booking cancelled"));
    }

    @DeleteMapping("/{bookingId}")
    public ResponseEntity<ApiResponse<Void>> deleteBooking(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long bookingId) {
        User currentUser = userService.findByEmail(userDetails.getUsername());
        bookingService.deleteBooking(bookingId, currentUser);
        return ResponseEntity.ok(ApiResponse.success(null, "Booking deleted successfully"));
    }

    // ========== PRIVATE HELPERS ==========

    private BookingResponse convertToResponse(Booking booking) {
        BookingResponse response = new BookingResponse();
        response.setId(booking.getId());
        response.setBookingReference(booking.getBookingReference());
        response.setUserId(booking.getUser().getId());
        response.setUserFullName(booking.getUser().getFullName());
        response.setUserEmail(booking.getUser().getEmail());
        response.setUserPhoneNumber(booking.getUser().getPhoneNumber());
        response.setCheckInDate(booking.getCheckInDate());
        response.setCheckOutDate(booking.getCheckOutDate());
        response.setGroupSize(booking.getGroupSize());
        response.setVehicleRegistration(booking.getVehicleRegistration());
        response.setPaymentMethod(booking.getPaymentMethod());
        response.setAmount(booking.getAmount());
        response.setPaymentStatus(booking.getPaymentStatus());
        response.setBookingStatus(booking.getBookingStatus());
        response.setPaymentReference(booking.getPaymentReference());
        response.setCreatedAt(booking.getCreatedAt());
        return response;
    }
}