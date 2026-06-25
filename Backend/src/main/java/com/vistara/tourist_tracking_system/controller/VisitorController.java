package com.vistara.tourist_tracking_system.controller;

import com.vistara.tourist_tracking_system.dto.ActiveSessionResponse;
import com.vistara.tourist_tracking_system.dto.ApiResponse;
import com.vistara.tourist_tracking_system.model.User;
import com.vistara.tourist_tracking_system.model.VisitorSession;
import com.vistara.tourist_tracking_system.service.UserService;
import com.vistara.tourist_tracking_system.service.VisitorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/visitor")
@RequiredArgsConstructor
public class VisitorController {

    private final VisitorService visitorService;
    private final UserService userService;

    @GetMapping("/active-session")
    public ResponseEntity<ApiResponse<ActiveSessionResponse>> getActiveSession(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByEmail(userDetails.getUsername());
        VisitorSession session = visitorService.findActiveSession(user);

        ActiveSessionResponse response = visitorService.convertToActiveSessionResponse(session);
        return ResponseEntity.ok(ApiResponse.success(response, "Active session retrieved"));
    }
}