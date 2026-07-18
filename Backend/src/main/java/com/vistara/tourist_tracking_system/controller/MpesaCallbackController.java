package com.vistara.tourist_tracking_system.controller;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.vistara.tourist_tracking_system.dto.MpesaCallbackPayload;
import com.vistara.tourist_tracking_system.model.Booking;
import com.vistara.tourist_tracking_system.service.BookingService;
import com.vistara.tourist_tracking_system.service.NotificationService;
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
    private final NotificationService notificationService;

    @PostMapping("/stk-callback")
    public ResponseEntity<Map<String, String>> stkCallback(@RequestBody String callbackJson) {
        log.info("======= M-PESA CALLBACK RECEIVED =======");
        log.info("Raw callback: {}", callbackJson);

        Map<String, String> response = new HashMap<>();

        try {
            // Create ObjectMapper with proper configuration
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            // Parse the callback payload
            MpesaCallbackPayload payload = objectMapper.readValue(callbackJson, MpesaCallbackPayload.class);

            // Validate payload structure
            if (payload.getBody() == null || payload.getBody().getStkCallback() == null) {
                log.error("Invalid callback payload structure");
                response.put("ResultCode", "1");
                response.put("ResultDesc", "Invalid payload");
                return ResponseEntity.badRequest().body(response);
            }

            MpesaCallbackPayload.StkCallback stkCallback = payload.getBody().getStkCallback();

            String checkoutRequestId = stkCallback.getCheckoutRequestId();
            Integer resultCode = stkCallback.getResultCode();
            String resultDesc = stkCallback.getResultDesc();

            log.info("CheckoutRequestID: {}, ResultCode: {}, ResultDesc: {}",
                    checkoutRequestId, resultCode, resultDesc);

            // Validate CheckoutRequestID
            if (checkoutRequestId == null || checkoutRequestId.isEmpty()) {
                log.error("CheckoutRequestID is null or empty");
                response.put("ResultCode", "1");
                response.put("ResultDesc", "Missing CheckoutRequestID");
                return ResponseEntity.badRequest().body(response);
            }

            // Find the booking by payment tracking ID
            Booking booking = bookingService.findByPaymentTrackingId(checkoutRequestId);

            if (booking == null) {
                log.warn("No booking found for CheckoutRequestID: {}", checkoutRequestId);
                // Return success to M-Pesa even if booking not found
                response.put("ResultCode", "0");
                response.put("ResultDesc", "Success");
                return ResponseEntity.ok(response);
            }

            log.info("Found booking: ID={}, Reference={}, Current Status={}",
                    booking.getId(), booking.getBookingReference(), booking.getPaymentStatus());

            // Check if payment was successful (ResultCode 0 = Success)
            if (resultCode != null && resultCode == 0) {
                // Extract payment details from callback metadata
                String receiptNo = null;
                String phoneNumber = null;
                Double amount = null;
                String transactionDate = null;

                if (stkCallback.getCallbackMetadata() != null
                        && stkCallback.getCallbackMetadata().getItem() != null) {

                    for (MpesaCallbackPayload.Item item : stkCallback.getCallbackMetadata().getItem()) {
                        String itemName = item.getName();
                        Object value = item.getValue();

                        log.debug("Metadata Item: {} = {}", itemName, value);

                        if ("MpesaReceiptNumber".equals(itemName)) {
                            receiptNo = value != null ? value.toString() : null;
                        } else if ("PhoneNumber".equals(itemName)) {
                            phoneNumber = value != null ? value.toString() : null;
                        } else if ("Amount".equals(itemName)) {
                            amount = value != null ? Double.valueOf(value.toString()) : null;
                        } else if ("TransactionDate".equals(itemName)) {
                            transactionDate = value != null ? value.toString() : null;
                        }
                    }
                }

                // Update booking payment status to PAID
                bookingService.updatePaymentStatus(checkoutRequestId, receiptNo, "PAID");

                // Send success notification
                String notificationMessage = String.format(
                        "Payment of KES %.2f for booking %s has been confirmed successfully. Receipt: %s",
                        amount != null ? amount : booking.getAmount(),
                        booking.getBookingReference(),
                        receiptNo != null ? receiptNo : "N/A"
                );

                notificationService.createNotification(
                        booking.getUser(),
                        "Payment Successful ✅",
                        notificationMessage,
                        "PAYMENT",
                        booking.getId(),
                        false
                );

                // Notify admin
                notificationService.createNotificationByEmail(
                        "admin@vistara.com",
                        "Payment Confirmed",
                        "Booking " + booking.getBookingReference() + " has been paid. Receipt: " + receiptNo,
                        "PAYMENT",
                        booking.getId(),
                        false
                );

                log.info("✅ Payment successful for booking {}: Receipt={}, Amount={}, Phone={}",
                        booking.getBookingReference(), receiptNo, amount, phoneNumber);

            } else {
                // Payment failed
                String errorDesc = resultDesc != null ? resultDesc : "Payment failed";
                bookingService.updatePaymentStatus(checkoutRequestId, null, "FAILED");

                // Send failure notification
                notificationService.createNotification(
                        booking.getUser(),
                        "Payment Failed ❌",
                        "Your payment for booking " + booking.getBookingReference() + " has failed. Reason: " + errorDesc,
                        "PAYMENT",
                        booking.getId(),
                        false
                );

                // Notify admin
                notificationService.createNotificationByEmail(
                        "admin@vistara.com",
                        "Payment Failed",
                        "Booking " + booking.getBookingReference() + " payment failed. Reason: " + errorDesc,
                        "PAYMENT",
                        booking.getId(),
                        false
                );

                log.warn("❌ Payment failed for booking {}: {} (ResultCode: {})",
                        booking.getBookingReference(), errorDesc, resultCode);
            }

            // Always return success to M-Pesa
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