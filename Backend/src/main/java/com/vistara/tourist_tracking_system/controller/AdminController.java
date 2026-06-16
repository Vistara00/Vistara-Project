package com.vistara.tourist_tracking_system.controller;

import com.vistara.tourist_tracking_system.dto.*;
import com.vistara.tourist_tracking_system.exception.DuplicateResourceException;
import com.vistara.tourist_tracking_system.model.Booking;
import com.vistara.tourist_tracking_system.model.EmergencyAlert;
import com.vistara.tourist_tracking_system.model.User;
import com.vistara.tourist_tracking_system.model.VisitorSession;
import com.vistara.tourist_tracking_system.repository.EmergencyAlertRepository;
import com.vistara.tourist_tracking_system.repository.UserRepository;
import com.vistara.tourist_tracking_system.repository.VisitorSessionRepository;
import com.vistara.tourist_tracking_system.service.BookingService;
import com.vistara.tourist_tracking_system.service.EmergencyService;
import com.vistara.tourist_tracking_system.service.UserService;
import com.vistara.tourist_tracking_system.service.VisitorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

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
    private final EmergencyService emergencyService;
    private final VisitorService visitorService;
    private final BookingService bookingService;
    private final UserService userService;
    private final JavaMailSender mailSender;

    @PutMapping("/visitor/{visitorId}")
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

    @GetMapping("/active-visitors")
    public ResponseEntity<ApiResponse<?>> getActiveVisitors() {
        List<VisitorSession> activeSessions = sessionRepository.findByActiveTrue();
        return ResponseEntity.ok(ApiResponse.success(activeSessions, "Active visitors retrieved"));
    }

    @GetMapping("/dashboard/stats")
    public ResponseEntity<ApiResponse<?>> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("activeVisitors", sessionRepository.findByActiveTrue().size());
        stats.put("pendingAlerts", alertRepository.findByAlertStatus(EmergencyAlert.AlertStatus.PENDING).size());
        stats.put("totalUsers", userRepository.count());
        stats.put("todayCheckins", sessionRepository.findByCheckInTimeAfter(LocalDateTime.now().minusHours(24)).size());
        return ResponseEntity.ok(ApiResponse.success(stats, "Dashboard stats retrieved"));
    }

    @PutMapping("/assign-ranger/{alertId}/{rangerId}")
    public ResponseEntity<ApiResponse<?>> assignRanger(
            @PathVariable Long alertId,
            @PathVariable Long rangerId) {
        User ranger = userRepository.findById(rangerId)
                .orElseThrow(() -> new DuplicateResourceException("Ranger not found"));
        EmergencyAlert alert = emergencyService.assignRanger(alertId, ranger);
        return ResponseEntity.ok(ApiResponse.success(alert, "Ranger assigned successfully"));
    }

    @PutMapping("/resolve-alert/{alertId}")
    public ResponseEntity<ApiResponse<?>> resolveAlert(
            @PathVariable Long alertId,
            @RequestParam String notes) {
        EmergencyAlert alert = emergencyService.resolveAlert(alertId, notes);
        return ResponseEntity.ok(ApiResponse.success(alert, "Alert resolved"));
    }

    @PostMapping("/checkin")
    public ResponseEntity<ApiResponse<VisitorSession>> adminCheckIn(
            @AuthenticationPrincipal UserDetails adminDetails,
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
            @AuthenticationPrincipal UserDetails adminDetails,
            @Valid @RequestBody AdminCheckOutRequest request) {
        User admin = userService.findByEmail(adminDetails.getUsername());
        VisitorSession session = visitorService.checkOutByAdmin(request.getSessionId(), request.getNotes(), admin);
        return ResponseEntity.ok(ApiResponse.success(session, "Check‑out successful"));
    }

    @PostMapping("/bookings/cash-booking")
    public ResponseEntity<ApiResponse<BookingResponse>> createCashBooking(
            @AuthenticationPrincipal UserDetails adminDetails,
            @Valid @RequestBody CashBookingRequest request) {

        User tourist = userService.createTourist(
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
        List<Booking> bookings;
        if (status != null && !status.isEmpty()) {
            bookings = bookingService.getBookingsByStatus(status);
        } else {
            bookings = bookingService.getAllBookings();
        }
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

    @PostMapping("/mpesa-callback")
    public ResponseEntity<String> mpesaCallback(@RequestBody String callbackJson) {
        System.out.println("M-Pesa callback received: " + callbackJson);
        return ResponseEntity.ok("{\"ResultCode\":0,\"ResultDesc\":\"Success\"}");
    }

    @GetMapping("/test-mail")
    public ResponseEntity<String> testMail() {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom("lawrencedenzel60@gmail.com");
            msg.setTo("lawrencedenzel60@gmail.com");
            msg.setSubject("Test");
            msg.setText("Hello");
            mailSender.send(msg);
            return ResponseEntity.ok("Email sent successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed: " + e.getMessage());
        }
    }

    private BookingResponse convertToResponse(Booking booking) {
        BookingResponse response = new BookingResponse();
        response.setId(booking.getId());
        response.setBookingReference(booking.getBookingReference());
        response.setUserId(booking.getUser().getId());
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
}