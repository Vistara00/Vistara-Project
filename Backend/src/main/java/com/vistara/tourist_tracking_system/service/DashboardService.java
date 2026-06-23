package com.vistara.tourist_tracking_system.service;

import com.vistara.tourist_tracking_system.dto.*;
import com.vistara.tourist_tracking_system.model.EmergencyAlert;
import com.vistara.tourist_tracking_system.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final EmergencyAlertRepository alertRepository;
    private final VisitorSessionRepository sessionRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;

    public DashboardStats getDashboardStats() {
        LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime startOfWeek = LocalDateTime.now().minusDays(30);

        // 1. Emergency alert breakdown
        List<Object[]> alertStats = alertRepository.countByStatusAndPriority();
        List<EmergencyAlertBreakdown> alertBreakdown = new ArrayList<>();
        for (Object[] row : alertStats) {
            String status = ((EmergencyAlert.AlertStatus) row[0]).name();
            String priority = ((EmergencyAlert.AlertPriority) row[1]).name();
            Long count = (Long) row[2];
            alertBreakdown.add(new EmergencyAlertBreakdown(status, priority, count));
        }

        // 2. Active visitors
        long activeVisitors = sessionRepository.countByActiveTrue();

        // 3. Total users
        long totalUsers = userRepository.count();

        // 4. Today check-ins / check-outs
        long todayCheckins = sessionRepository.countCheckInsToday(startOfDay);
        long todayCheckouts = sessionRepository.countCheckOutsToday(startOfDay);

        // 5. Daily revenue for the last 30 days
        List<Object[]> revenueData = bookingRepository.getDailyRevenue(startOfWeek);
        double averageRevenue = 0.0;
        if (!revenueData.isEmpty()) {
            double totalRevenue = revenueData.stream()
                    .mapToDouble(row -> ((Number) row[1]).doubleValue())
                    .sum();
            averageRevenue = totalRevenue / revenueData.size();
        }

        // 6. Daily attendance (last 30 days) – handle date conversion safely
        List<Object[]> dailyAtt = sessionRepository.findDailyAttendance(startOfWeek);
        List<DailyStat> attendanceDaily = dailyAtt.stream()
                .map(row -> {
                    LocalDate date = toLocalDate(row[0]);
                    long count = ((Number) row[1]).longValue();
                    return new DailyStat(date, count);
                })
                .collect(Collectors.toList());

        // 7. Weekly attendance (last 30 days)
        List<Object[]> weeklyAtt = sessionRepository.findWeeklyAttendance(startOfWeek);
        List<WeeklyStat> attendanceWeekly = weeklyAtt.stream()
                .map(row -> new WeeklyStat(((Number) row[0]).intValue(), ((Number) row[1]).longValue()))
                .collect(Collectors.toList());

        // 8. Daily revenue for chart (safe date conversion)
        List<DailyStat> revenueDaily = revenueData.stream()
                .map(row -> {
                    LocalDate date = toLocalDate(row[0]);
                    long amount = ((Number) row[1]).longValue();
                    return new DailyStat(date, amount);
                })
                .collect(Collectors.toList());

        return new DashboardStats(
                alertBreakdown,
                activeVisitors,
                totalUsers,
                todayCheckins,
                todayCheckouts,
                averageRevenue,
                attendanceDaily,
                revenueDaily,
                attendanceWeekly
        );
    }

    // Helper method to safely convert database date to LocalDate
    private LocalDate toLocalDate(Object dateObj) {
        if (dateObj instanceof LocalDate) {
            return (LocalDate) dateObj;
        } else if (dateObj instanceof Date) {
            return ((Date) dateObj).toLocalDate();
        } else if (dateObj instanceof java.sql.Timestamp) {
            return ((java.sql.Timestamp) dateObj).toLocalDateTime().toLocalDate();
        } else if (dateObj instanceof String) {
            return LocalDate.parse((String) dateObj);
        } else {
            // fallback: use toString and parse
            return LocalDate.parse(dateObj.toString());
        }
    }
}