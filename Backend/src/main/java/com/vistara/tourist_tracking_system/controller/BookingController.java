package com.vistara.tourist_tracking_system.controller;

import com.vistara.tourist_tracking_system.dto.*;
import com.vistara.tourist_tracking_system.model.Booking;
import com.vistara.tourist_tracking_system.model.User;
import com.vistara.tourist_tracking_system.service.BookingService;
import com.vistara.tourist_tracking_system.service.QRCodeService;
import com.vistara.tourist_tracking_system.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private final UserService userService;
    private final QRCodeService qrCodeService;

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
     * ✅ Get booking with QR code for successful/paid booking
     */
    @GetMapping("/{bookingId}/qr")
    public ResponseEntity<ApiResponse<QRCodeResponse>> getBookingQRCode(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long bookingId) {
        User user = userService.findByEmail(userDetails.getUsername());
        Booking booking = bookingService.getBookingByIdAndUser(bookingId, user);

        // Only generate QR code if booking is confirmed and paid
        if (!"PAID".equals(booking.getPaymentStatus()) ||
                !"CONFIRMED".equals(booking.getBookingStatus())) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("QR code is only available for confirmed and paid bookings"));
        }

        // Generate QR code
        String qrCodeBase64 = qrCodeService.generateBookingQRCode(booking.getId(), booking.getBookingReference());

        QRCodeResponse response = QRCodeResponse.builder()
                .bookingId(booking.getId())
                .bookingReference(booking.getBookingReference())
                .qrCodeBase64(qrCodeBase64)
                .visitorName(booking.getUser().getFullName())
                .checkInDate(booking.getCheckInDate().toString())
                .checkOutDate(booking.getCheckOutDate().toString())
                .paymentStatus(booking.getPaymentStatus())
                .bookingStatus(booking.getBookingStatus())
                .build();

        return ResponseEntity.ok(ApiResponse.success(response, "QR Code generated successfully"));
    }

    /**
     * ✅ Scan QR code - Ranger endpoint to scan and get booking details
     */
    @PostMapping("/scan-qr")
    public ResponseEntity<ApiResponse<BookingResponse>> scanQRCode(
            @RequestBody Map<String, String> request) {
        String qrData = request.get("qrData");

        if (qrData == null || qrData.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("QR data is required"));
        }

        // Parse QR data format: VISTARA|BOOKING|{bookingId}|{bookingReference}
        String[] parts = qrData.split("\\|");
        if (parts.length < 4 || !"VISTARA".equals(parts[0]) || !"BOOKING".equals(parts[1])) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Invalid QR code format"));
        }

        try {
            Long bookingId = Long.parseLong(parts[2]);
            String bookingReference = parts[3];

            Booking booking = bookingService.getBookingById(bookingId);

            // Verify the booking reference matches
            if (!booking.getBookingReference().equals(bookingReference)) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Invalid QR code: booking reference mismatch"));
            }

            // Check if booking is paid and confirmed
            if (!"PAID".equals(booking.getPaymentStatus()) ||
                    !"CONFIRMED".equals(booking.getBookingStatus())) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Booking is not confirmed or payment not completed"));
            }

            BookingResponse response = convertToResponse(booking);

            // Check if visitor is already checked in
            // You can add logic here to check if there's an active session

            return ResponseEntity.ok(ApiResponse.success(response, "Booking details retrieved successfully"));
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Invalid QR code: invalid booking ID"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to process QR code: " + e.getMessage()));
        }
    }

    /**
     * ✅ Auto check-in from QR scan (Ranger endpoint)
     */
    @PostMapping("/qr-checkin")
    public ResponseEntity<ApiResponse<Map<String, Object>>> qrCheckin(
            @RequestBody Map<String, String> request) {
        String qrData = request.get("qrData");

        if (qrData == null || qrData.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("QR data is required"));
        }

        // Parse QR data
        String[] parts = qrData.split("\\|");
        if (parts.length < 4 || !"VISTARA".equals(parts[0]) || !"BOOKING".equals(parts[1])) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Invalid QR code format"));
        }

        try {
            Long bookingId = Long.parseLong(parts[2]);
            String bookingReference = parts[3];

            Booking booking = bookingService.getBookingById(bookingId);

            // Verify booking reference
            if (!booking.getBookingReference().equals(bookingReference)) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Invalid QR code: booking reference mismatch"));
            }

            // Check if booking is paid and confirmed
            if (!"PAID".equals(booking.getPaymentStatus()) ||
                    !"CONFIRMED".equals(booking.getBookingStatus())) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Booking is not confirmed or payment not completed"));
            }

            // Check if user already has an active session
            // This would call your check-in service

            Map<String, Object> response = new HashMap<>();
            response.put("bookingId", booking.getId());
            response.put("bookingReference", booking.getBookingReference());
            response.put("visitorName", booking.getUser().getFullName());
            response.put("checkInDate", booking.getCheckInDate());
            response.put("checkOutDate", booking.getCheckOutDate());
            response.put("message", "Check-in successful. Welcome to Vistara Park!");

            return ResponseEntity.ok(ApiResponse.success(response, "Check-in successful"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to process check-in: " + e.getMessage()));
        }
    }

    /**
     * ✅ Get only the payment status of a booking
     */
    @GetMapping("/{bookingId}/payment-status")
    public ResponseEntity<ApiResponse<PaymentStatusResponse>> getPaymentStatus(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long bookingId) {
        User user = userService.findByEmail(userDetails.getUsername());
        Booking booking = bookingService.getBookingByIdAndUser(bookingId, user);

        PaymentStatusResponse response = new PaymentStatusResponse();
        response.setBookingId(booking.getId());
        response.setPaymentStatus(booking.getPaymentStatus());

        return ResponseEntity.ok(ApiResponse.success(response, "Payment status retrieved"));
    }

    /**
     * ✅ Get payment status by booking reference (public, no auth required)
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
        response.setPaymentStatus(booking.getPaymentStatus());

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