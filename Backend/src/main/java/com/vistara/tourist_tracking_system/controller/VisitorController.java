package com.vistara.tourist_tracking_system.controller;

import com.vistara.tourist_tracking_system.dto.ApiResponse;
import com.vistara.tourist_tracking_system.dto.CheckInRequest;
import com.vistara.tourist_tracking_system.dto.CheckOutRequest;
import com.vistara.tourist_tracking_system.model.User;
import com.vistara.tourist_tracking_system.model.VisitorSession;
import com.vistara.tourist_tracking_system.service.UserService;
import com.vistara.tourist_tracking_system.service.VisitorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/visitor")
@RequiredArgsConstructor
public class VisitorController {

    private final VisitorService visitorService;
    private final UserService userService;

    @PostMapping("/checkin")
    public ResponseEntity<ApiResponse<VisitorSession>> checkIn(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CheckInRequest request) {

        User user = userService.findByEmail(userDetails.getUsername());
        VisitorSession session = visitorService.checkIn(user, request);
        return ResponseEntity.ok(ApiResponse.success(session, "Check-in successful. Booking created."));
    }

    // ✅ ADD THIS METHOD
    @PostMapping("/checkout")
    public ResponseEntity<ApiResponse<VisitorSession>> checkOut(
            @Valid @RequestBody CheckOutRequest request) {
        VisitorSession session = visitorService.checkOut(request);
        return ResponseEntity.ok(ApiResponse.success(session, "Check-out successful. Safe travels!"));
    }
}