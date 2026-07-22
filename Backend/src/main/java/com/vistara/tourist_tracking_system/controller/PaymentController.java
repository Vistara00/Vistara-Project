package com.vistara.tourist_tracking_system.controller;

import com.vistara.tourist_tracking_system.dto.ApiResponse;
import com.vistara.tourist_tracking_system.dto.MpesaStkRequest;
import com.vistara.tourist_tracking_system.dto.MpesaStkResponse;
import com.vistara.tourist_tracking_system.model.Booking;
import com.vistara.tourist_tracking_system.model.User;
import com.vistara.tourist_tracking_system.service.BookingService;
import com.vistara.tourist_tracking_system.service.MpesaService;
import com.vistara.tourist_tracking_system.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final BookingService bookingService;
    private final UserService userService;
    private final MpesaService mpesaService;  // Direct injection, no Optional

    @Value("${mpesa.enabled:true}")
    private boolean mpesaEnabled;

    @PostMapping("/mpesa/stkpush")
    public ResponseEntity<ApiResponse<?>> initiateStkPush(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody MpesaStkRequest request) {

        log.info("========================================");
        log.info("📱 STK Push Request Received");
        log.info("📱 Phone: {}", request.getPhoneNumber());
        log.info("📱 Amount: {}", request.getAmount());
        log.info("📱 Account Reference: {}", request.getAccountReference());
        log.info("📱 Transaction Desc: {}", request.getTransactionDesc());
        log.info("📱 M-Pesa Enabled: {}", mpesaEnabled);
        log.info("========================================");

        // Check if M-Pesa is enabled
        if (!mpesaEnabled) {
            log.warn("⚠️ M-Pesa is disabled. Returning mock response.");
            MpesaStkResponse mockResponse = new MpesaStkResponse();
            mockResponse.setCheckoutRequestId("MOCK-" + System.currentTimeMillis());
            mockResponse.setMerchantRequestId("MOCK-MERCHANT-" + System.currentTimeMillis());
            mockResponse.setResponseCode("0");
            mockResponse.setResponseDescription("Success. M-Pesa is disabled in this environment.");

            Map<String, Object> response = new HashMap<>();
            response.put("mock", true);
            response.put("message", "M-Pesa is disabled. This is a mock response.");
            response.put("data", mockResponse);

            log.info("✅ Mock STK Push response sent (M-Pesa disabled)");
            return ResponseEntity.ok(ApiResponse.success(response, "Mock M-Pesa STK Push"));
        }

        try {
            // Get the authenticated user
            User user = userService.findByEmail(userDetails.getUsername());
            log.info("👤 Authenticated user: {}", user.getEmail());

            // Find booking by reference
            Booking booking = bookingService.findByBookingReference(request.getAccountReference());
            if (booking == null) {
                log.error("❌ Booking not found for reference: {}", request.getAccountReference());
                return ResponseEntity.badRequest().body(ApiResponse.error("Booking not found"));
            }
            log.info("📋 Booking found: ID={}, Reference={}, Status={}",
                    booking.getId(), booking.getBookingReference(), booking.getPaymentStatus());

            // Check authorization
            if (!booking.getUser().getId().equals(user.getId()) &&
                    !user.getRole().equals(User.Role.ADMIN) &&
                    !user.getRole().equals(User.Role.RANGER)) {
                log.warn("⛔ Unauthorized payment attempt for booking {} by user {}",
                        booking.getBookingReference(), user.getEmail());
                return ResponseEntity.status(403).body(ApiResponse.error("Not authorized to make payment for this booking"));
            }

            // Check if booking is already paid
            if ("PAID".equals(booking.getPaymentStatus())) {
                log.warn("⚠️ Booking {} is already paid", booking.getBookingReference());
                return ResponseEntity.badRequest().body(ApiResponse.error("Booking is already paid"));
            }

            // Initiate STK Push
            log.info("🚀 Initiating STK Push for booking: {}", booking.getBookingReference());
            MpesaStkResponse response = mpesaService.stkPush(request);

            log.info("📡 STK Push Response:");
            log.info("   - MerchantRequestId: {}", response.getMerchantRequestId());
            log.info("   - CheckoutRequestId: {}", response.getCheckoutRequestId());
            log.info("   - ResponseCode: {}", response.getResponseCode());
            log.info("   - ResponseDescription: {}", response.getResponseDescription());
            log.info("   - CustomerMessage: {}", response.getCustomerMessage());

            // Save the checkout request ID to booking
            if (response.getCheckoutRequestId() != null) {
                bookingService.updatePaymentTrackingId(booking.getId(), response.getCheckoutRequestId());
                log.info("✅ CheckoutRequestId saved to booking: {}", response.getCheckoutRequestId());
            }

            // Check if STK Push was successful
            if ("0".equals(response.getResponseCode())) {
                log.info("✅ STK Push initiated successfully for booking: {}", booking.getBookingReference());
                log.info("📱 M-Pesa prompt should have been sent to phone: {}", request.getPhoneNumber());
            } else {
                log.warn("⚠️ STK Push returned non-zero response code: {} - {}",
                        response.getResponseCode(), response.getResponseDescription());
            }

            return ResponseEntity.ok(ApiResponse.success(response, "STK Push initiated successfully"));

        } catch (Exception e) {
            log.error("❌ STK Push failed for booking {}: {}", request.getAccountReference(), e.getMessage(), e);
            return ResponseEntity.status(500).body(ApiResponse.error("STK Push failed: " + e.getMessage()));
        }
    }
}