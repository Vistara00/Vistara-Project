package com.vistara.tourist_tracking_system.controller;

import com.vistara.tourist_tracking_system.dto.ActiveSessionResponse;
import com.vistara.tourist_tracking_system.dto.ApiResponse;
import com.vistara.tourist_tracking_system.dto.VisitorSessionResponse;
import com.vistara.tourist_tracking_system.model.User;
import com.vistara.tourist_tracking_system.model.VisitorSession;
import com.vistara.tourist_tracking_system.service.UserService;
import com.vistara.tourist_tracking_system.service.VisitorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/visitor")
@RequiredArgsConstructor
public class VisitorController {

    private final VisitorService visitorService;
    private final UserService userService;

    /**
     * Get the authenticated visitor's active session
     * Returns detailed session information including sessionId
     */
    @GetMapping("/active-session")
    public ResponseEntity<ApiResponse<ActiveSessionResponse>> getActiveSession(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByEmail(userDetails.getUsername());
        VisitorSession session = visitorService.findActiveSession(user);

        ActiveSessionResponse response = visitorService.convertToActiveSessionResponse(session);
        return ResponseEntity.ok(ApiResponse.success(response, "Active session retrieved"));
    }

    /**
     * Get the authenticated visitor's active session with more details
     * Alias for /active-session with consistent naming
     */
    @GetMapping("/my-active-session")
    public ResponseEntity<ApiResponse<ActiveSessionResponse>> getMyActiveSession(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByEmail(userDetails.getUsername());
        VisitorSession session = visitorService.findActiveSession(user);

        if (session == null) {
            log.info("No active session found for user: {}", user.getEmail());
            return ResponseEntity.ok(ApiResponse.success(null, "No active session found"));
        }

        ActiveSessionResponse response = visitorService.convertToActiveSessionResponse(session);
        log.info("Active session retrieved for user: {}, sessionId: {}", user.getEmail(), session.getId());
        return ResponseEntity.ok(ApiResponse.success(response, "Active session retrieved successfully"));
    }

    /**
     * Check if the authenticated visitor has an active session
     */
    @GetMapping("/has-active-session")
    public ResponseEntity<ApiResponse<Boolean>> hasActiveSession(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByEmail(userDetails.getUsername());
        VisitorSession session = visitorService.findActiveSession(user);
        return ResponseEntity.ok(ApiResponse.success(session != null, "Active session status checked"));
    }

    /**
     * Get the authenticated visitor's active session as VisitorSessionResponse
     * Returns sessionId explicitly in the response
     */
    @GetMapping("/active-session-detailed")
    public ResponseEntity<ApiResponse<VisitorSessionResponse>> getActiveSessionDetailed(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByEmail(userDetails.getUsername());
        VisitorSession session = visitorService.findActiveSession(user);

        if (session == null) {
            return ResponseEntity.ok(ApiResponse.success(null, "No active session found"));
        }

        VisitorSessionResponse response = visitorService.convertToSessionResponse(session);
        return ResponseEntity.ok(ApiResponse.success(response, "Active session details retrieved"));
    }
}