package com.vistara.tourist_tracking_system.controller;

import com.vistara.tourist_tracking_system.dto.ApiResponse;
import com.vistara.tourist_tracking_system.dto.MpesaStkRequest;
import com.vistara.tourist_tracking_system.dto.MpesaStkResponse;
import com.vistara.tourist_tracking_system.model.Booking;
import com.vistara.tourist_tracking_system.model.User;
import com.vistara.tourist_tracking_system.service.BookingService;
import com.vistara.tourist_tracking_system.service.MpesaService;
import com.vistara.tourist_tracking_system.service.MockMpesaService;
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
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final BookingService bookingService;
    private final UserService userService;
    private final Optional<MpesaService> mpesaService;
    private final Optional<MockMpesaService> mockMpesaService;

    // ✅ FIXED: Correct boolean syntax
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

            log.info("✅ Mock STK Push response sent");
            return ResponseEntity.ok(ApiResponse.success(response, "Mock M-Pesa STK Push"));
        }

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

        try {
            log.info("🚀 Initiating STK Push for booking: {}", booking.getBookingReference());

            if (mpesaService.isPresent()) {
                log.info("📡 Using real M-Pesa service");
                MpesaStkResponse response = mpesaService.get().stkPush(request);

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
            } else if (mockMpesaService.isPresent()) {
                log.warn("⚠️ M-Pesa service not available. Using mock.");
                MpesaStkResponse mockResponse = mockMpesaService.get().stkPush(request);

                log.info("📡 Mock STK Push Response:");
                log.info("   - CheckoutRequestId: {}", mockResponse.getCheckoutRequestId());
                log.info("   - ResponseCode: {}", mockResponse.getResponseCode());
                log.info("   - ResponseDescription: {}", mockResponse.getResponseDescription());

                bookingService.updatePaymentTrackingId(booking.getId(), mockResponse.getCheckoutRequestId());

                Map<String, Object> response = new HashMap<>();
                response.put("mock", true);
                response.put("message", "M-Pesa service not available. This is a mock response.");
                response.put("data", mockResponse);

                log.info("✅ Mock STK Push response sent");
                return ResponseEntity.ok(ApiResponse.success(response, "Mock M-Pesa STK Push"));
            } else {
                log.error("❌ No M-Pesa service available");
                return ResponseEntity.status(500).body(ApiResponse.error("M-Pesa service not available"));
            }
        } catch (Exception e) {
            log.error("❌ STK Push failed for booking {}: {}", booking.getBookingReference(), e.getMessage(), e);
            return ResponseEntity.status(500).body(ApiResponse.error("STK Push failed: " + e.getMessage()));
        }
    }
}