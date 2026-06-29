package com.vistara.tourist_tracking_system.controller;

import com.vistara.tourist_tracking_system.dto.ApiResponse;
import com.vistara.tourist_tracking_system.dto.MpesaStkRequest;
import com.vistara.tourist_tracking_system.dto.MpesaStkResponse;
import com.vistara.tourist_tracking_system.model.Booking;
import com.vistara.tourist_tracking_system.service.BookingService;
import com.vistara.tourist_tracking_system.service.MpesaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final MpesaService mpesaService;
    private final BookingService bookingService;

    @PostMapping("/mpesa/stkpush")
    public ResponseEntity<ApiResponse<MpesaStkResponse>> initiateStkPush(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody MpesaStkRequest request) {

        log.info("Initiating STK Push for accountReference: {}", request.getAccountReference());

        // Find the booking by accountReference (should be bookingReference)
        Booking booking = bookingService.findByBookingReference(request.getAccountReference());

        if (booking == null) {
            log.error("Booking not found for reference: {}", request.getAccountReference());
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Booking not found with reference: " + request.getAccountReference())
            );
        }

        log.info("Found booking: ID={}, Reference={}, Status={}",
                booking.getId(), booking.getBookingReference(), booking.getPaymentStatus());

        // Check if booking is already paid
        if ("PAID".equals(booking.getPaymentStatus())) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Booking is already paid")
            );
        }

        // Initiate STK Push
        MpesaStkResponse response = mpesaService.stkPush(request);

        // Store the CheckoutRequestID in the booking
        bookingService.updatePaymentTrackingId(booking.getId(), response.getCheckoutRequestId());

        log.info("STK Push initiated successfully. CheckoutRequestID: {}, Booking: {}",
                response.getCheckoutRequestId(), booking.getBookingReference());

        return ResponseEntity.ok(ApiResponse.success(response, "STK Push sent. Check your phone."));
    }
}