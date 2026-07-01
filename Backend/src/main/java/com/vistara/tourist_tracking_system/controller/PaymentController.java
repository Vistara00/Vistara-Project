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

import java.util.List;
import java.util.stream.Collectors;

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

        log.info("========================================");
        log.info("📱 STK Push Request Received");
        log.info("Account Reference: {}", request.getAccountReference());
        log.info("Phone Number: {}", request.getPhoneNumber());
        log.info("Amount: {}", request.getAmount());
        log.info("========================================");

        // Find the booking by accountReference
        Booking booking = bookingService.findByBookingReference(request.getAccountReference());

        if (booking == null) {
            log.error("❌ Booking not found for reference: {}", request.getAccountReference());

            // Try to find by partial match (helpful for debugging)
            log.info("Attempting to find booking by partial match...");
            List<Booking> allBookings = bookingService.getAllBookings();
            List<String> references = allBookings.stream()
                    .map(Booking::getBookingReference)
                    .collect(Collectors.toList());
            log.info("Available booking references: {}", references);

            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Booking not found with reference: " + request.getAccountReference() +
                            ". Available references: " + references)
            );
        }

        log.info("✅ Booking found: ID={}, Reference={}, Status={}, Amount={}",
                booking.getId(),
                booking.getBookingReference(),
                booking.getPaymentStatus(),
                booking.getAmount());

        // Check if booking is already paid
        if ("PAID".equals(booking.getPaymentStatus())) {
            log.warn("⚠️ Booking {} is already paid", booking.getBookingReference());
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Booking is already paid")
            );
        }

        // Ensure the account reference matches the booking reference
        String accountReference = booking.getBookingReference();
        log.info("Using account reference: {}", accountReference);

        // Update the request with the correct account reference
        MpesaStkRequest mpesaRequest = new MpesaStkRequest();
        mpesaRequest.setPhoneNumber(request.getPhoneNumber());
        mpesaRequest.setAmount(booking.getAmount().intValue()); // Use the booking amount
        mpesaRequest.setAccountReference(accountReference);
        mpesaRequest.setTransactionDesc("Vistara Park Entry Payment");

        try {
            // Initiate STK Push
            MpesaStkResponse response = mpesaService.stkPush(mpesaRequest);

            // Store the CheckoutRequestID in the booking
            if (response != null && response.getCheckoutRequestId() != null) {
                bookingService.updatePaymentTrackingId(booking.getId(), response.getCheckoutRequestId());
                log.info("✅ STK Push initiated successfully. CheckoutRequestID: {}, Booking: {}",
                        response.getCheckoutRequestId(), booking.getBookingReference());
            } else {
                log.error("❌ Failed to get CheckoutRequestID from M-Pesa response");
                return ResponseEntity.badRequest().body(
                        ApiResponse.error("Failed to initiate STK Push: No CheckoutRequestID received")
                );
            }

            return ResponseEntity.ok(ApiResponse.success(response, "STK Push sent. Check your phone."));

        } catch (Exception e) {
            log.error("❌ STK Push failed for booking {}: {}", booking.getBookingReference(), e.getMessage(), e);
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("STK Push failed: " + e.getMessage())
            );
        }
    }
}