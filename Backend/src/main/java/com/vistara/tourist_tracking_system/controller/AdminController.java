package com.vistara.tourist_tracking_system.controller;

import com.vistara.tourist_tracking_system.dto.*;
import com.vistara.tourist_tracking_system.exception.DuplicateResourceException;
import com.vistara.tourist_tracking_system.model.Booking;
import com.vistara.tourist_tracking_system.model.LocationTracking;
import com.vistara.tourist_tracking_system.model.Notification;
import com.vistara.tourist_tracking_system.model.User;
import com.vistara.tourist_tracking_system.model.VisitorSession;
import com.vistara.tourist_tracking_system.repository.EmergencyAlertRepository;
import com.vistara.tourist_tracking_system.repository.LocationTrackingRepository;
import com.vistara.tourist_tracking_system.repository.UserRepository;
import com.vistara.tourist_tracking_system.repository.VisitorSessionRepository;
import com.vistara.tourist_tracking_system.service.*;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final VisitorSessionRepository sessionRepository;
    private final EmergencyAlertRepository alertRepository;
    private final UserRepository userRepository;
    private final LocationTrackingRepository locationTrackingRepository;
    private final EmergencyService emergencyService;
    private final VisitorService visitorService;
    private final BookingService bookingService;
    private final UserService userService;
    private final DashboardService dashboardService;
    private final NotificationService notificationService;
    private final TrackingService trackingService;
    private static final Logger log = LoggerFactory.getLogger(AdminController.class);

    // ========== USER MANAGEMENT ==========

    @PutMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<UserResponseDTO>> adminUpdateUser(
            @PathVariable Long userId,
            @Valid @RequestBody AdminUpdateUserRequest request) {
        UserResponseDTO updated = userService.adminUpdateUser(userId, request);
        return ResponseEntity.ok(ApiResponse.success(updated, "User updated by admin"));
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserResponseDTO>>> getAllUsers() {
        List<UserResponseDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success(users, "All users retrieved successfully"));
    }

    // ========== VISITOR MONITORING ==========

    @GetMapping("/active-visitors")
    public ResponseEntity<ApiResponse<List<ActiveVisitorResponse>>> getActiveVisitors() {
        List<VisitorSession> activeSessions = sessionRepository.findByActiveTrue();
        List<ActiveVisitorResponse> responses = activeSessions.stream()
                .map(this::convertToActiveVisitorResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(responses, "Active visitors retrieved"));
    }

    @GetMapping("/dashboard/stats")
    public ResponseEntity<ApiResponse<DashboardStats>> getDashboardStats() {
        DashboardStats stats = dashboardService.getDashboardStats();
        return ResponseEntity.ok(ApiResponse.success(stats, "Dashboard stats retrieved"));
    }

    // ========== VISITOR TRACKING ==========

    @GetMapping("/visitors-locations")
    public ResponseEntity<ApiResponse<List<LocationTracking>>> getAllActiveVisitorsLocations() {
        List<LocationTracking> locations = locationTrackingRepository.findRecentLocations(
                LocalDateTime.now().minusMinutes(5)
        );
        return ResponseEntity.ok(ApiResponse.success(locations, "Active visitors locations retrieved"));
    }

    @GetMapping("/visitor-tracking/{sessionId}")
    public ResponseEntity<ApiResponse<List<LocationTracking>>> getVisitorTrackingHistory(
            @PathVariable Long sessionId,
            @RequestParam(required = false) LocalDateTime from,
            @RequestParam(required = false) LocalDateTime to) {
        if (from == null) {
            from = LocalDateTime.now().minusHours(24);
        }
        if (to == null) {
            to = LocalDateTime.now();
        }
        List<LocationTracking> locations = trackingService.getLocationHistory(sessionId, from, to);
        return ResponseEntity.ok(ApiResponse.success(locations, "Visitor tracking history retrieved"));
    }

    @GetMapping("/visitor-location/{sessionId}")
    public ResponseEntity<ApiResponse<LocationTracking>> getVisitorLastLocation(
            @PathVariable Long sessionId) {
        LocationTracking location = trackingService.getLastLocation(sessionId);
        return ResponseEntity.ok(ApiResponse.success(location, "Last location retrieved"));
    }

    @GetMapping("/live-tracking")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getLiveTracking() {
        List<Object[]> results = locationTrackingRepository.findLiveTrackingData();
        List<Map<String, Object>> trackingData = results.stream()
                .map(row -> {
                    Map<String, Object> data = new HashMap<>();
                    data.put("sessionId", row[0]);
                    data.put("visitorName", row[1]);
                    data.put("visitorEmail", row[2]);
                    data.put("latitude", row[3]);
                    data.put("longitude", row[4]);
                    data.put("lastUpdate", row[5]);
                    data.put("groupSize", row[6]);
                    data.put("vehicleRegistration", row[7]);
                    data.put("sosTriggered", row[8]);
                    return data;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(trackingData, "Live tracking data retrieved"));
    }

    @GetMapping("/visitor-tracking-details/{sessionId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getVisitorTrackingDetails(
            @PathVariable Long sessionId) {
        Map<String, Object> details = new HashMap<>();
        VisitorSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));
        LocationTracking lastLocation = locationTrackingRepository.findTopBySessionOrderByTimestampDesc(session);
        List<LocationTracking> history = trackingService.getLocationHistory(
                sessionId,
                LocalDateTime.now().minusHours(24),
                LocalDateTime.now()
        );
        details.put("session", session);
        details.put("visitor", session.getUser());
        details.put("lastLocation", lastLocation);
        details.put("locationHistory", history);
        details.put("totalLocations", history.size());
        details.put("booking", session.getBooking());
        return ResponseEntity.ok(ApiResponse.success(details, "Visitor tracking details retrieved"));
    }

    // ========== RANGER MANAGEMENT ==========

    @GetMapping("/rangers")
    public ResponseEntity<ApiResponse<List<UserResponseDTO>>> getAllRangers() {
        List<UserResponseDTO> rangers = userService.getAllRangers();
        return ResponseEntity.ok(ApiResponse.success(rangers, "All rangers retrieved successfully"));
    }

    @GetMapping("/rangers/available")
    public ResponseEntity<ApiResponse<List<UserResponseDTO>>> getAvailableRangers() {
        List<UserResponseDTO> availableRangers = userService.getAvailableRangers();
        return ResponseEntity.ok(ApiResponse.success(availableRangers, "Available rangers retrieved successfully"));
    }

    @GetMapping("/rangers/{rangerId}")
    public ResponseEntity<ApiResponse<UserResponseDTO>> getRangerById(
            @PathVariable Long rangerId) {
        UserResponseDTO ranger = userService.getUserById(rangerId);
        return ResponseEntity.ok(ApiResponse.success(ranger, "Ranger retrieved successfully"));
    }

    @PutMapping("/assign-ranger/{alertId}/{rangerId}")
    public ResponseEntity<ApiResponse<EmergencyAlertResponse>> assignRanger(
            @PathVariable Long alertId,
            @PathVariable Long rangerId) {
        User ranger = userRepository.findById(rangerId)
                .orElseThrow(() -> new DuplicateResourceException("Ranger not found"));
        EmergencyAlertResponse alert = emergencyService.assignRanger(alertId, ranger);
        return ResponseEntity.ok(ApiResponse.success(alert, "Ranger assigned successfully"));
    }

    @PutMapping("/resolve-alert/{alertId}")
    public ResponseEntity<ApiResponse<EmergencyAlertResponse>> resolveAlert(
            @PathVariable Long alertId,
            @RequestBody Map<String, String> request) {
        String notes = request.get("notes");
        EmergencyAlertResponse alert = emergencyService.resolveAlert(alertId, notes);
        return ResponseEntity.ok(ApiResponse.success(alert, "Alert resolved"));
    }

    @GetMapping("/alerts/active")
    public ResponseEntity<ApiResponse<List<EmergencyAlertResponse>>> getActiveAlerts() {
        List<EmergencyAlertResponse> alerts = emergencyService.getActiveAlerts();
        return ResponseEntity.ok(ApiResponse.success(alerts, "Active alerts retrieved"));
    }

    @GetMapping("/alerts")
    public ResponseEntity<ApiResponse<List<EmergencyAlertResponse>>> getAlerts(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) LocalDateTime from,
            @RequestParam(required = false) LocalDateTime to) {
        List<EmergencyAlertResponse> alerts = emergencyService.getAlerts(status, from, to);
        return ResponseEntity.ok(ApiResponse.success(alerts, "Alerts retrieved"));
    }

    @GetMapping("/rangers/{rangerId}/alerts")
    public ResponseEntity<ApiResponse<List<EmergencyAlertResponse>>> getRangerAlerts(
            @PathVariable Long rangerId) {
        List<EmergencyAlertResponse> alerts = emergencyService.getAlertsByRanger(rangerId);
        return ResponseEntity.ok(ApiResponse.success(alerts, "Ranger alerts retrieved"));
    }

    // ========== CHECK-IN / CHECK-OUT ==========

    @PostMapping("/checkin")
    public ResponseEntity<ApiResponse<VisitorSessionResponse>> adminCheckIn(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails adminDetails,
            @Valid @RequestBody AdminCheckInRequest request) {
        User admin = userService.findByEmail(adminDetails.getUsername());

        if (request.getBookingId() != null && request.getBookingId() > 0) {
            Booking booking = bookingService.getBookingById(request.getBookingId());
            if (!"PAID".equals(booking.getPaymentStatus())) {
                return ResponseEntity.badRequest().body(
                        ApiResponse.error("Booking payment is not PAID. Current status: " + booking.getPaymentStatus())
                );
            }
            if (!"CONFIRMED".equals(booking.getBookingStatus())) {
                return ResponseEntity.badRequest().body(
                        ApiResponse.error("Booking is not CONFIRMED. Current status: " + booking.getBookingStatus())
                );
            }
        }

        VisitorSession session = visitorService.checkInByAdmin(
                request.getBookingId(),
                request.getWalkInUserId(),
                request.getVehicleRegistrationOverride(),
                request.getNotes(),
                admin);

        VisitorSessionResponse response = visitorService.convertToSessionResponse(session);
        return ResponseEntity.ok(ApiResponse.success(response, "Check‑in successful"));
    }

    @PostMapping("/checkout")
    public ResponseEntity<ApiResponse<VisitorSession>> adminCheckOut(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails adminDetails,
            @Valid @RequestBody AdminCheckOutRequest request) {
        User admin = userService.findByEmail(adminDetails.getUsername());
        VisitorSession session = visitorService.checkOutByAdmin(request.getSessionId(), request.getNotes(), admin);
        return ResponseEntity.ok(ApiResponse.success(session, "Check‑out successful"));
    }

    // ========== ACTIVE VISITORS WITH SESSION DETAILS ==========

    @GetMapping("/active-sessions")
    public ResponseEntity<ApiResponse<List<VisitorSessionResponse>>> getActiveSessions() {
        List<VisitorSession> activeSessions = sessionRepository.findByActiveTrue();
        List<VisitorSessionResponse> responses = activeSessions.stream()
                .map(visitorService::convertToSessionResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(responses, "Active sessions retrieved"));
    }

    @GetMapping("/active-sessions-with-location")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getActiveSessionsWithLocation() {
        List<VisitorSession> activeSessions = sessionRepository.findByActiveTrue();
        List<Map<String, Object>> responses = activeSessions.stream()
                .map(session -> {
                    Map<String, Object> data = new HashMap<>();
                    data.put("sessionId", session.getId());
                    data.put("visitorName", session.getUser().getFullName());
                    data.put("visitorEmail", session.getUser().getEmail());
                    data.put("visitorPhone", session.getUser().getPhoneNumber());
                    data.put("checkInTime", session.getCheckInTime());
                    data.put("groupSize", session.getGroupSize());
                    data.put("vehicleRegistration", session.getVehicleRegistration());
                    data.put("sosTriggered", session.isSosTriggered());
                    data.put("hasEmergency", session.isHasEmergency());

                    LocationTracking lastLocation = locationTrackingRepository
                            .findTopBySessionOrderByTimestampDesc(session);
                    if (lastLocation != null) {
                        data.put("lastLatitude", lastLocation.getLatitude());
                        data.put("lastLongitude", lastLocation.getLongitude());
                        data.put("lastLocationTime", lastLocation.getTimestamp());
                    }

                    if (session.getBooking() != null) {
                        data.put("bookingReference", session.getBooking().getBookingReference());
                        data.put("bookingStatus", session.getBooking().getBookingStatus());
                    }

                    return data;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(responses, "Active sessions with location retrieved"));
    }

    @GetMapping("/active-session/user/{userId}")
    public ResponseEntity<ApiResponse<VisitorSessionResponse>> getActiveSessionByUserId(
            @PathVariable Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        VisitorSession session = visitorService.findActiveSession(user);
        if (session == null) {
            return ResponseEntity.ok(ApiResponse.success(null, "No active session found for this user"));
        }

        VisitorSessionResponse response = visitorService.convertToSessionResponse(session);
        return ResponseEntity.ok(ApiResponse.success(response, "Active session retrieved"));
    }

    // ========== BOOKING MANAGEMENT ==========

    @PostMapping("/bookings/cash-booking")
    public ResponseEntity<ApiResponse<BookingResponse>> createCashBooking(
            @AuthenticationPrincipal UserDetails adminDetails,
            @Valid @RequestBody CashBookingRequest request) {
        User tourist = userService.findOrCreateTourist(
                request.getFullName(),
                request.getEmail(),
                request.getPhoneNumber()
        );
        Booking booking = bookingService.createConfirmedBooking(
                tourist,
                request.getCheckInDate(),
                request.getCheckOutDate(),
                request.getNumberOfPeople(),
                request.getVehicleRegistration(),
                request.getAmount(),
                "CASH",
                request.getNotes()
        );
        BookingResponse response = convertToResponse(booking);
        return ResponseEntity.ok(ApiResponse.success(response, "Cash booking created successfully. Visitor can now be checked in."));
    }

    @GetMapping("/bookings")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getAllBookings(
            @RequestParam(required = false) String status) {
        List<Booking> bookings = (status != null && !status.isEmpty())
                ? bookingService.getBookingsByStatus(status)
                : bookingService.getAllBookings();
        List<BookingResponse> responses = bookings.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(responses, "All bookings retrieved"));
    }

    @GetMapping("/bookings/{bookingId}")
    public ResponseEntity<ApiResponse<BookingResponse>> getBookingById(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails adminDetails,
            @PathVariable Long bookingId) {
        Booking booking = bookingService.getBookingById(bookingId);
        BookingResponse response = convertToResponse(booking);
        return ResponseEntity.ok(ApiResponse.success(response, "Booking retrieved successfully"));
    }

    @PostMapping("/bookings/{bookingId}/confirm-payment")
    public ResponseEntity<ApiResponse<Void>> confirmPayment(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails adminDetails,
            @PathVariable Long bookingId,
            @RequestParam String paymentReference) {

        User admin = userService.findByEmail(adminDetails.getUsername());
        Booking booking = bookingService.getBookingById(bookingId);

        if ("PAID".equals(booking.getPaymentStatus()) ||
                "CONFIRMED".equals(booking.getBookingStatus())) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Booking is already confirmed/paid")
            );
        }

        bookingService.confirmPayment(bookingId, paymentReference, "PAID");
        log.info("Admin {} manually confirmed payment for booking {}", admin.getEmail(), bookingId);

        return ResponseEntity.ok(ApiResponse.success(null, "Payment confirmed, booking now confirmed"));
    }

    @DeleteMapping("/bookings/{bookingId}")
    public ResponseEntity<ApiResponse<Void>> adminDeleteBooking(
            @AuthenticationPrincipal UserDetails adminDetails,
            @PathVariable Long bookingId) {
        User admin = userService.findByEmail(adminDetails.getUsername());
        bookingService.deleteBooking(bookingId, admin);
        return ResponseEntity.ok(ApiResponse.success(null, "Booking deleted by admin"));
    }

    // ========== M-PESA CALLBACK ==========

    @PostMapping("/mpesa-callback")
    public ResponseEntity<String> mpesaCallback(@RequestBody String callbackJson) {
        System.out.println("M-Pesa callback received: " + callbackJson);
        return ResponseEntity.ok("{\"ResultCode\":0,\"ResultDesc\":\"Success\"}");
    }

    @PostMapping("/bookings/mpesa-booking")
    public ResponseEntity<ApiResponse<BookingResponse>> createMpesaBooking(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails adminDetails,
            @Valid @RequestBody AdminMpesaBookingRequest request) {
        User tourist = userService.findOrCreateTourist(
                request.getFullName(),
                request.getEmail(),
                request.getPhoneNumber()
        );
        Booking booking = bookingService.createBookingWithMpesa(tourist, request);
        BookingResponse response = convertToResponse(booking);
        return ResponseEntity.ok(ApiResponse.success(
                response,
                "M-Pesa payment initiated. Visitor will receive a prompt on their phone."
        ));
    }

    // ========== BROADCAST MANAGEMENT ==========

    @PostMapping("/broadcast")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> broadcastNotification(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails adminDetails,
            @Valid @RequestBody BroadcastRequest request) {

        List<Notification> notifications;

        if (request.getUserId() != null && request.getUserId() > 0) {
            Notification notification = notificationService.broadcastToUser(
                    request.getUserId(),
                    request.getTitle(),
                    request.getMessage()
            );
            notifications = List.of(notification);
        } else if (request.getBroadcastType() == BroadcastRequest.BroadcastType.ACTIVE_VISITORS) {
            notifications = notificationService.broadcastToActiveVisitors(
                    request.getTitle(),
                    request.getMessage()
            );
        } else {
            notifications = notificationService.broadcastToAllUsers(
                    request.getTitle(),
                    request.getMessage()
            );
        }

        List<NotificationResponse> responses = notifications.stream()
                .map(this::convertToNotificationResponse)
                .collect(Collectors.toList());

        String message = getBroadcastMessage(request);
        return ResponseEntity.ok(ApiResponse.success(responses, message));
    }

    @PostMapping("/broadcast/active-visitors")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> broadcastToActiveVisitors(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails adminDetails,
            @Valid @RequestBody BroadcastRequest request) {

        List<Notification> notifications = notificationService.broadcastToActiveVisitors(
                request.getTitle(),
                request.getMessage()
        );

        List<NotificationResponse> responses = notifications.stream()
                .map(this::convertToNotificationResponse)
                .collect(Collectors.toList());

        String message = responses.isEmpty()
                ? "No active visitors to notify"
                : "Broadcast sent to " + responses.size() + " active visitors";

        return ResponseEntity.ok(ApiResponse.success(responses, message));
    }

    @GetMapping("/broadcasts/all")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getAllBroadcasts(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails adminDetails) {

        List<NotificationResponse> allBroadcasts = notificationService.getAllBroadcasts();

        log.info("Admin {} retrieved all broadcasts. Total: {}", adminDetails.getUsername(), allBroadcasts.size());
        return ResponseEntity.ok(ApiResponse.success(allBroadcasts, "All broadcasts retrieved successfully"));
    }

    // ========== PRIVATE HELPERS ==========

    private String getBroadcastMessage(BroadcastRequest request) {
        if (request.getUserId() != null && request.getUserId() > 0) {
            return "Broadcast sent to user successfully";
        }
        if (request.getBroadcastType() == BroadcastRequest.BroadcastType.ACTIVE_VISITORS) {
            return "Broadcast sent to active visitors successfully";
        }
        return "Broadcast sent to all users successfully";
    }

    private BookingResponse convertToResponse(Booking booking) {
        BookingResponse response = new BookingResponse();
        response.setId(booking.getId());
        response.setBookingReference(booking.getBookingReference());
        response.setUserId(booking.getUser().getId());
        response.setUserFullName(booking.getUser().getFullName());
        response.setUserEmail(booking.getUser().getEmail());
        response.setUserPhoneNumber(booking.getUser().getPhoneNumber());
        response.setCheckInDate(booking.getCheckInDate());
        response.setCheckOutDate(booking.getCheckOutDate());
        response.setGroupSize(booking.getGroupSize());
        response.setVehicleRegistration(booking.getVehicleRegistration());
        response.setPaymentMethod(booking.getPaymentMethod());
        response.setAmount(booking.getAmount());
        response.setPaymentStatus(booking.getPaymentStatus());
        response.setBookingStatus(booking.getBookingStatus());
        response.setPaymentReference(booking.getPaymentReference());
        response.setCreatedAt(booking.getCreatedAt());
        return response;
    }

    private NotificationResponse convertToNotificationResponse(Notification notification) {
        NotificationResponse response = new NotificationResponse();
        response.setId(notification.getId());
        response.setTitle(notification.getTitle());
        response.setMessage(notification.getMessage());
        response.setType(notification.getType());
        response.setRead(notification.isRead());
        response.setCreatedAt(notification.getCreatedAt());
        return response;
    }

    private ActiveVisitorResponse convertToActiveVisitorResponse(VisitorSession session) {
        Booking booking = session.getBooking();
        return ActiveVisitorResponse.builder()
                .id(session.getId())
                .bookingReference(booking != null ? booking.getBookingReference() : null)
                .userFullName(session.getUser().getFullName())
                .userEmail(session.getUser().getEmail())
                .userPhoneNumber(session.getUser().getPhoneNumber())
                .amount(booking != null ? booking.getAmount() : null)
                .checkInDate(booking != null ? booking.getCheckInDate() : null)
                .checkOutDate(booking != null ? booking.getCheckOutDate() : null)
                .bookingStatus(booking != null ? booking.getBookingStatus() : null)
                .vehicleRegistration(session.getVehicleRegistration())
                .groupSize(session.getGroupSize())
                .checkInTime(session.getCheckInTime())
                .sosTriggered(session.isSosTriggered())
                .hasEmergency(session.isHasEmergency())
                .notes(session.getNotes())
                .build();
    }
}