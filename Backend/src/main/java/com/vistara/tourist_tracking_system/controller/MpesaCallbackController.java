package com.vistara.tourist_tracking_system.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vistara.tourist_tracking_system.dto.MpesaCallbackPayload;
import com.vistara.tourist_tracking_system.model.Booking;
import com.vistara.tourist_tracking_system.service.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/payments/mpesa")
@RequiredArgsConstructor
public class MpesaCallbackController {

    private final BookingService bookingService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping("/stk-callback")
    public ResponseEntity<Map<String, String>> stkCallback(@RequestBody String callbackJson) {
        log.info("======= M-PESA CALLBACK RECEIVED =======");
        log.info("Raw callback: {}", callbackJson);

        Map<String, String> response = new HashMap<>();

        try {
            MpesaCallbackPayload payload = objectMapper.readValue(callbackJson, MpesaCallbackPayload.class);

            if (payload.getBody() == null || payload.getBody().getStkCallback() == null) {
                log.error("Invalid callback payload structure");
                response.put("ResultCode", "1");
                response.put("ResultDesc", "Invalid payload");
                return ResponseEntity.badRequest().body(response);
            }

            MpesaCallbackPayload.StkCallback stkCallback = payload.getBody().getStkCallback();

            String checkoutRequestId = stkCallback.getCheckoutRequestId();
            Integer resultCode = stkCallback.getResultCode();

            log.info("CheckoutRequestID: {}, ResultCode: {}", checkoutRequestId, resultCode);

            if (checkoutRequestId == null || checkoutRequestId.isEmpty()) {
                log.error("CheckoutRequestID is null or empty");
                response.put("ResultCode", "1");
                response.put("ResultDesc", "Missing CheckoutRequestID");
                return ResponseEntity.badRequest().body(response);
            }

            Booking booking = bookingService.findByPaymentTrackingId(checkoutRequestId);

            if (booking == null) {
                log.warn("No booking found for CheckoutRequestID: {}", checkoutRequestId);
                response.put("ResultCode", "0");
                response.put("ResultDesc", "Success");
                return ResponseEntity.ok(response);
            }

            log.info("Found booking: ID={}, Reference={}, Current Status={}",
                    booking.getId(), booking.getBookingReference(), booking.getPaymentStatus());

            if (resultCode != null && resultCode == 0) {
                String receiptNo = null;
                if (stkCallback.getCallbackMetadata() != null && stkCallback.getCallbackMetadata().getItem() != null) {
                    for (MpesaCallbackPayload.Item item : stkCallback.getCallbackMetadata().getItem()) {
                        String itemName = item.getName();
                        if ("MpesaReceiptNumber".equals(itemName)) {
                            Object value = item.getValue();
                            receiptNo = value != null ? value.toString() : null;
                            break;
                        }
                    }
                }

                // This will now send notifications via BookingService.updatePaymentStatus()
                bookingService.updatePaymentStatus(checkoutRequestId, receiptNo, "PAID");
                log.info("✅ Booking {} updated to PAID. Receipt: {}", booking.getId(), receiptNo);
            } else {
                String errorDesc = stkCallback.getResultDesc() != null ? stkCallback.getResultDesc() : "Unknown error";
                bookingService.updatePaymentStatus(checkoutRequestId, null, "FAILED");
                log.warn("❌ Payment failed for booking {}: {}", booking.getId(), errorDesc);
            }

            response.put("ResultCode", "0");
            response.put("ResultDesc", "Success");

        } catch (Exception e) {
            log.error("Error processing M-Pesa callback: ", e);
            response.put("ResultCode", "1");
            response.put("ResultDesc", "Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }

        log.info("======= M-PESA CALLBACK PROCESSED =======");
        return ResponseEntity.ok(response);
    }
}