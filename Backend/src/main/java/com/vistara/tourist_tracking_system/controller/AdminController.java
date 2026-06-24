package com.vistara.tourist_tracking_system.controller;

import com.vistara.tourist_tracking_system.dto.*;
import com.vistara.tourist_tracking_system.exception.DuplicateResourceException;
import com.vistara.tourist_tracking_system.model.Booking;
import com.vistara.tourist_tracking_system.model.EmergencyAlert;
import com.vistara.tourist_tracking_system.model.Notification;
import com.vistara.tourist_tracking_system.model.User;
import com.vistara.tourist_tracking_system.model.VisitorSession;
import com.vistara.tourist_tracking_system.repository.EmergencyAlertRepository;
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

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final VisitorSessionRepository sessionRepository;
    private final EmergencyAlertRepository alertRepository;
    private final UserRepository userRepository;
    private final EmergencyService emergencyService;
    private final VisitorService visitorService;
    private final BookingService bookingService;
    private final UserService userService;
    private final DashboardService dashboardService;
    private final NotificationService notificationService;  // ADDED

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
    public ResponseEntity<ApiResponse<List<VisitorSession>>> getActiveVisitors() {
        List<VisitorSession> activeSessions = sessionRepository.findByActiveTrue();
        return ResponseEntity.ok(ApiResponse.success(activeSessions, "Active visitors retrieved"));
    }

    @GetMapping("/dashboard/stats")
    public ResponseEntity<ApiResponse<DashboardStats>> getDashboardStats() {
        DashboardStats stats = dashboardService.getDashboardStats();
        return ResponseEntity.ok(ApiResponse.success(stats, "Dashboard stats retrieved"));
    }

    // ========== EMERGENCY MANAGEMENT ==========

    @PutMapping("/assign-ranger/{alertId}/{rangerId}")
    public ResponseEntity<ApiResponse<EmergencyAlert>> assignRanger(
            @PathVariable Long alertId,
            @PathVariable Long rangerId) {
        User ranger = userRepository.findById(rangerId)
                .orElseThrow(() -> new DuplicateResourceException("Ranger not found"));
        EmergencyAlert alert = emergencyService.assignRanger(alertId, ranger);
        return ResponseEntity.ok(ApiResponse.success(alert, "Ranger assigned successfully"));
    }

    @PutMapping("/resolve-alert/{alertId}")
    public ResponseEntity<ApiResponse<EmergencyAlert>> resolveAlert(
            @PathVariable Long alertId,
            @RequestParam String notes) {
        EmergencyAlert alert = emergencyService.resolveAlert(alertId, notes);
        return ResponseEntity.ok(ApiResponse.success(alert, "Alert resolved"));
    }

    // ========== CHECK-IN / CHECK-OUT ==========

    @PostMapping("/checkin")
    public ResponseEntity<ApiResponse<VisitorSession>> adminCheckIn(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails adminDetails,
            @Valid @RequestBody AdminCheckInRequest request) {
        User admin = userService.findByEmail(adminDetails.getUsername());
        VisitorSession session = visitorService.checkInByAdmin(
                request.getBookingId(),
                request.getWalkInUserId(),
                request.getVehicleRegistrationOverride(),
                request.getNotes(),
                admin);
        return ResponseEntity.ok(ApiResponse.success(session, "Check‑in successful"));
    }

    @PostMapping("/checkout")
    public ResponseEntity<ApiResponse<VisitorSession>> adminCheckOut(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails adminDetails,
            @Valid @RequestBody AdminCheckOutRequest request) {
        User admin = userService.findByEmail(adminDetails.getUsername());
        VisitorSession session = visitorService.checkOutByAdmin(request.getSessionId(), request.getNotes(), admin);
        return ResponseEntity.ok(ApiResponse.success(session, "Check‑out successful"));
    }

    // ========== BOOKING MANAGEMENT ==========

    @PostMapping("/bookings/cash-booking")
    public ResponseEntity<ApiResponse<BookingResponse>> createCashBooking(
            @AuthenticationPrincipal UserDetails adminDetails,
            @Valid @RequestBody CashBookingRequest request) {

        // Find existing user or create a new one
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

    @PostMapping("/bookings/{bookingId}/confirm-payment")
    public ResponseEntity<ApiResponse<Void>> confirmPayment(
            @PathVariable Long bookingId,
            @RequestParam String paymentReference) {
        bookingService.confirmPayment(bookingId, paymentReference, "PAID");
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

        // Find existing user or create a new one
        User tourist = userService.findOrCreateTourist(
                request.getFullName(),
                request.getEmail(),
                request.getPhoneNumber()
        );

        // Create booking and initiate M-Pesa payment
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
            // Broadcast to specific user
            Notification notification = notificationService.broadcastToUser(
                    request.getUserId(),
                    request.getTitle(),
                    request.getMessage()
            );
            notifications = List.of(notification);
        } else {
            // Broadcast to all users
            notifications = notificationService.broadcastToAllUsers(
                    request.getTitle(),
                    request.getMessage()
            );
        }

        List<NotificationResponse> responses = notifications.stream()
                .map(this::convertToNotificationResponse)
                .collect(Collectors.toList());

        String message = request.getUserId() != null && request.getUserId() > 0
                ? "Broadcast sent to user successfully"
                : "Broadcast sent to all users successfully";

        return ResponseEntity.ok(ApiResponse.success(responses, message));
    }

    @GetMapping("/broadcasts")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getAllBroadcasts(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails adminDetails) {
        List<NotificationResponse> broadcasts = notificationService.getAllBroadcasts();
        return ResponseEntity.ok(ApiResponse.success(broadcasts, "All broadcasts retrieved"));
    }

    // ========== PRIVATE HELPERS ==========

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
        response.setBroadcast(notification.isBroadcast());
        response.setReferenceId(notification.getReferenceId());
        response.setCreatedAt(notification.getCreatedAt());
        return response;
    }
}