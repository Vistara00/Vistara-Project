package com.vistara.tourist_tracking_system.controller;

import com.vistara.tourist_tracking_system.dto.ApiResponse;
import com.vistara.tourist_tracking_system.dto.MpesaStkRequest;
import com.vistara.tourist_tracking_system.dto.MpesaStkResponse;
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
        MpesaStkResponse response = mpesaService.stkPush(request);
        return ResponseEntity.ok(ApiResponse.success(response, "STK Push sent. Check your phone."));
    }
}