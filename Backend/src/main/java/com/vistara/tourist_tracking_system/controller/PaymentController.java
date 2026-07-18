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

    @Value("${mpesa.enabled:false}")
    private boolean mpesaEnabled;

    @PostMapping("/mpesa/stkpush")
    public ResponseEntity<ApiResponse<?>> initiateStkPush(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody MpesaStkRequest request) {

        if (!mpesaEnabled) {
            log.warn("M-Pesa is disabled. Returning mock response.");
            MpesaStkResponse mockResponse = new MpesaStkResponse();
            mockResponse.setCheckoutRequestId("MOCK-" + System.currentTimeMillis());
            mockResponse.setMerchantRequestId("MOCK-MERCHANT-" + System.currentTimeMillis());
            mockResponse.setResponseCode("0");
            mockResponse.setResponseDescription("Success. M-Pesa is disabled in this environment.");

            Map<String, Object> response = new HashMap<>();
            response.put("mock", true);
            response.put("message", "M-Pesa is disabled. This is a mock response.");
            response.put("data", mockResponse);

            return ResponseEntity.ok(ApiResponse.success(response, "Mock M-Pesa STK Push"));
        }

        User user = userService.findByEmail(userDetails.getUsername());

        Booking booking = bookingService.findByBookingReference(request.getAccountReference());
        if (booking == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Booking not found"));
        }

        if (!booking.getUser().getId().equals(user.getId()) &&
                !user.getRole().equals(User.Role.ADMIN) &&
                !user.getRole().equals(User.Role.RANGER)) {
            return ResponseEntity.status(403).body(ApiResponse.error("Not authorized to make payment for this booking"));
        }

        try {
            if (mpesaService.isPresent()) {
                MpesaStkResponse response = mpesaService.get().stkPush(request);
                bookingService.updatePaymentTrackingId(booking.getId(), response.getCheckoutRequestId());
                log.info("STK Push initiated for booking: {}", booking.getBookingReference());
                return ResponseEntity.ok(ApiResponse.success(response, "STK Push initiated successfully"));
            } else {
                log.warn("M-Pesa service not available. Using mock.");
                MpesaStkResponse mockResponse = new MpesaStkResponse();
                mockResponse.setCheckoutRequestId("MOCK-" + System.currentTimeMillis());
                mockResponse.setMerchantRequestId("MOCK-MERCHANT-" + System.currentTimeMillis());
                mockResponse.setResponseCode("0");
                mockResponse.setResponseDescription("Success. Mock M-Pesa payment.");

                Map<String, Object> response = new HashMap<>();
                response.put("mock", true);
                response.put("message", "M-Pesa service not available. This is a mock response.");
                response.put("data", mockResponse);

                return ResponseEntity.ok(ApiResponse.success(response, "Mock M-Pesa STK Push"));
            }
        } catch (Exception e) {
            log.error("STK Push failed: {}", e.getMessage());
            return ResponseEntity.status(500).body(ApiResponse.error("STK Push failed: " + e.getMessage()));
        }
    }

    // ✅ REMOVE the duplicate callback method from this controller
    // The callback is handled by MpesaCallbackController
}