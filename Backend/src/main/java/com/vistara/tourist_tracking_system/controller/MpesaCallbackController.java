package com.vistara.tourist_tracking_system.controller;

import com.vistara.tourist_tracking_system.dto.MpesaCallbackPayload;
import com.vistara.tourist_tracking_system.model.Booking;
import com.vistara.tourist_tracking_system.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    private final BookingRepository bookingRepository;

    @PostMapping("/stk-callback")
    public ResponseEntity<Map<String, String>> stkCallback(@RequestBody MpesaCallbackPayload payload) {
        log.info("M-Pesa callback received: {}", payload);

        try {
            MpesaCallbackPayload.StkCallback stkCallback = payload.getBody().getStkCallback();
            String checkoutRequestId = stkCallback.getCheckoutRequestId();
            Integer resultCode = stkCallback.getResultCode();

            // Find the booking using the stored CheckoutRequestID
            Booking booking = bookingRepository.findByPaymentTrackingId(checkoutRequestId)
                    .orElseThrow(() -> new RuntimeException("No booking found for CheckoutRequestID: " + checkoutRequestId));

            if (resultCode == 0) {
                // Payment successful
                String receiptNo = null;
                if (stkCallback.getCallbackMetadata() != null) {
                    for (MpesaCallbackPayload.Item item : stkCallback.getCallbackMetadata().getItem()) {
                        if ("MpesaReceiptNumber".equals(item.getName())) {
                            receiptNo = item.getValue().toString();
                            break;
                        }
                    }
                }
                booking.setPaymentStatus(Booking.PaymentStatus.PAID);
                booking.setBookingStatus(Booking.BookingStatus.CONFIRMED);
                booking.setPaymentReference(receiptNo);
                bookingRepository.save(booking);
                log.info("Booking {} payment confirmed", booking.getId());
            } else {
                // Payment failed
                booking.setPaymentStatus(Booking.PaymentStatus.FAILED);
                bookingRepository.save(booking);
                log.warn("Payment failed for booking {}: {}", booking.getId(), stkCallback.getResultDesc());
            }
        } catch (Exception e) {
            log.error("Error processing M-Pesa callback", e);
        }

        // Always return success response to M-Pesa
        Map<String, String> response = new HashMap<>();
        response.put("ResultCode", "0");
        response.put("ResultDesc", "Success");
        return ResponseEntity.ok(response);
    }
}