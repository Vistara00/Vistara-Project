package com.vistara.tourist_tracking_system.controller;

import com.vistara.tourist_tracking_system.dto.ApiResponse;
import com.vistara.tourist_tracking_system.dto.BookingQRResponse;
import com.vistara.tourist_tracking_system.model.Booking;
import com.vistara.tourist_tracking_system.model.User;
import com.vistara.tourist_tracking_system.service.BookingService;
import com.vistara.tourist_tracking_system.service.QRCodeService;
import com.vistara.tourist_tracking_system.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
//@RequestMapping("/qr")  // ✅ Changed from /bookings to /qr
@RequiredArgsConstructor
public class QRCodeController {

    private final BookingService bookingService;
    private final QRCodeService qrCodeService;
    private final UserService userService;

    /**
     * Get QR Code for a booking
     * GET /qr/booking/{bookingId}
     */
    @GetMapping("/booking/{bookingId}")  // ✅ Full path: /qr/booking/{bookingId}
    public ResponseEntity<ApiResponse<BookingQRResponse>> getBookingQR(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long bookingId) {

        log.info("📱 QR Code request for booking: {}", bookingId);

        try {
            User currentUser = userService.findByEmail(userDetails.getUsername());
            Booking booking = bookingService.getBookingById(bookingId);

            // Role-based access control
            boolean isAdmin = currentUser.getRole() == User.Role.ADMIN;
            boolean isOwner = booking.getUser().getId().equals(currentUser.getId());
            boolean isRanger = currentUser.getRole() == User.Role.PARK_RANGER || currentUser.getRole() == User.Role.RANGER;

            if (!isAdmin && !isOwner && !isRanger) {
                return ResponseEntity.status(403).body(
                        ApiResponse.error("You don't have permission to view this QR code")
                );
            }

            // Generate QR Code
            String qrCodeBase64 = qrCodeService.generateQRCode(booking);

            BookingQRResponse response = BookingQRResponse.builder()
                    .bookingId(booking.getId())
                    .bookingReference(booking.getBookingReference())
                    .qrCodeBase64(qrCodeBase64)
                    .visitorName(booking.getUser().getFullName())
                    .checkInDate(booking.getCheckInDate())
                    .checkOutDate(booking.getCheckOutDate())
                    .paymentStatus(booking.getPaymentStatus())
                    .bookingStatus(booking.getBookingStatus())
                    .build();

            log.info("✅ QR Code generated for booking: {}", booking.getBookingReference());

            return ResponseEntity.ok(ApiResponse.success(response, "QR Code generated successfully"));

        } catch (Exception e) {
            log.error("❌ Failed to generate QR Code: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Failed to generate QR Code: " + e.getMessage())
            );
        }
    }

    /**
     * Get QR Code for user's own active booking
     * GET /qr/my-booking
     */
    @GetMapping("/my-booking")
    public ResponseEntity<ApiResponse<BookingQRResponse>> getMyBookingQR(
            @AuthenticationPrincipal UserDetails userDetails) {

        try {
            User currentUser = userService.findByEmail(userDetails.getUsername());

            // Find active booking for tourist
            Booking activeBooking = bookingService.findActiveBookingByUser(currentUser);
            if (activeBooking == null) {
                return ResponseEntity.ok(ApiResponse.success(null, "No active booking found"));
            }

            String qrCodeBase64 = qrCodeService.generateQRCode(activeBooking);

            BookingQRResponse response = BookingQRResponse.builder()
                    .bookingId(activeBooking.getId())
                    .bookingReference(activeBooking.getBookingReference())
                    .qrCodeBase64(qrCodeBase64)
                    .visitorName(activeBooking.getUser().getFullName())
                    .checkInDate(activeBooking.getCheckInDate())
                    .checkOutDate(activeBooking.getCheckOutDate())
                    .paymentStatus(activeBooking.getPaymentStatus())
                    .bookingStatus(activeBooking.getBookingStatus())
                    .build();

            return ResponseEntity.ok(ApiResponse.success(response, "QR Code retrieved successfully"));

        } catch (Exception e) {
            log.error("❌ Failed to get user's QR Code: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Failed to get QR Code: " + e.getMessage())
            );
        }
    }
}